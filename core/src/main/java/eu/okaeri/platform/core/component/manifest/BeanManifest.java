package eu.okaeri.platform.core.component.manifest;

import eu.okaeri.injector.Injector;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.platform.core.annotation.Bean;
import eu.okaeri.platform.core.annotation.DependsOn;
import eu.okaeri.platform.core.annotation.External;
import eu.okaeri.platform.core.annotation.Register;
import eu.okaeri.platform.core.component.ExternalResourceProvider;
import eu.okaeri.platform.core.component.creator.ComponentCreator;
import lombok.Data;
import lombok.NonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Data
public class BeanManifest {

    private static final Logger LOGGER = Logger.getLogger(BeanManifest.class.getName());

    public static BeanManifest of(@NonNull Parameter parameter) {
        return of(parameter, false);
    }

    public static BeanManifest of(@NonNull Parameter parameter, boolean readParamName) {

        Inject inject = parameter.getAnnotation(Inject.class);
        String name = (inject == null) ? (readParamName ? parameter.getName() : "") : inject.value();

        BeanManifest manifest = new BeanManifest();
        manifest.setType(parameter.getType());
        manifest.setName(name);
        manifest.setDepends(Collections.emptyList());
        manifest.setExternals(Collections.emptyList());
        manifest.setSource(BeanSource.INJECT);

        return manifest;
    }

    private static BeanManifest of(@NonNull Field field) {

        Inject inject = field.getAnnotation(Inject.class);
        String name = (inject == null) ? "" : inject.value();

        BeanManifest manifest = new BeanManifest();
        manifest.setName(name);
        manifest.setType(field.getType());
        manifest.setDepends(Collections.emptyList());
        manifest.setExternals(Collections.emptyList());
        manifest.setSource(BeanSource.INJECT);

        return manifest;
    }

    public static BeanManifest of(@NonNull Class<?> clazz, @NonNull ComponentCreator creator, boolean fullLoad) {

        if (!creator.isComponent(clazz)) {
            throw new IllegalArgumentException("Cannot create manifest of non-component " + clazz);
        }

        BeanManifest manifest = new BeanManifest();
        manifest.setType(clazz);
        manifest.setName(nameClass(clazz));
        manifest.setSource(BeanSource.COMPONENT);
        manifest.setFullLoad(fullLoad);

        List<External> externals = Arrays.asList(clazz.getAnnotationsByType(External.class));
        manifest.setExternals(externals);

        List<BeanManifest> depends = new ArrayList<>();
        manifest.setDepends(depends);

        depends.addAll(Arrays.stream(clazz.getAnnotationsByType(Register.class))
                .filter(Objects::nonNull)
                .map(reg -> BeanManifest.of(reg, creator))
                .collect(Collectors.toList()));

        depends.addAll(Arrays.stream(clazz.getAnnotationsByType(DependsOn.class))
                .filter(Objects::nonNull)
                .map(dependency -> BeanManifest.ofRequirement(dependency.type(), dependency.name()))
                .collect(Collectors.toList()));

        depends.addAll(Arrays.stream(clazz.getDeclaredMethods())
                .filter(creator::isComponentMethod)
                .map(method -> BeanManifest.of(manifest, method, creator))
                .collect(Collectors.toList()));

        boolean constructorDepends = false;
        for (Constructor<?> constructor : clazz.getConstructors()) {
            if (constructor.getAnnotation(Inject.class) != null) {
                depends.addAll(Arrays.stream(constructor.getParameters())
                        .map(param -> BeanManifest.of(param, true))
                        .collect(Collectors.toList()));
                constructorDepends = true;
                break;
            }
        }

        if (!constructorDepends) {
            depends.addAll(Arrays.stream(clazz.getDeclaredFields())
                    .filter(field -> field.getAnnotation(Inject.class) != null)
                    .map(BeanManifest::of)
                    .collect(Collectors.toList()));
        }

        return manifest;
    }

    private static BeanManifest ofRequirement(@NonNull Class<?> type, @NonNull String name) {

        BeanManifest manifest = new BeanManifest();
        manifest.setName(name);
        manifest.setType(type);
        manifest.setDepends(Collections.emptyList());
        manifest.setExternals(Collections.emptyList());
        manifest.setSource(BeanSource.INJECT);

        return manifest;
    }

    public static BeanManifest of(@NonNull BeanManifest parent, @NonNull Method method, @NonNull ComponentCreator creator) {

        Bean annotation = method.getAnnotation(Bean.class);
        if ((annotation == null) && !creator.isComponentMethod(method)) {
            throw new IllegalArgumentException("Cannot create BeanManifest from method without @Bean: " + method);
        }

        BeanManifest manifest = new BeanManifest();
        manifest.setType(method.getReturnType());
        manifest.setName((annotation == null) ? "" : annotation.value());
        manifest.setPreload(annotation != null && annotation.preload());

        manifest.setDepends(Arrays.stream(method.getParameters())
                .filter(parameter -> !creator.getRegistry().isDynamicParameter(parameter))
                .map(BeanManifest::of)
                .collect(Collectors.toList()));
        manifest.setExternals(Collections.emptyList());

        manifest.setSource(BeanSource.METHOD);
        manifest.setMethod(method);
        manifest.setParent(parent);

        return manifest;
    }

    public static BeanManifest of(@NonNull Register register, @NonNull ComponentCreator creator) {
        return of(register.value(), creator, false);
    }

    private String name;
    private Object object;
    private Class<?> type;
    private BeanSource source;
    private BeanManifest parent;
    private Method method;
    private boolean preload = false;
    private boolean fullLoad = false;
    private List<BeanManifest> depends;
    private List<External> externals;
    private Map<DependencyPair, Integer> failCounter = new HashMap<>();

    public BeanManifest withDepend(int pos, @NonNull BeanManifest beanManifest) {
        this.depends.add(pos, beanManifest);
        return this;
    }

    public BeanManifest withDepend(@NonNull BeanManifest beanManifest) {
        this.depends.add(beanManifest);
        return this;
    }

    public BeanManifest name(@NonNull String name) {
        this.name = name;
        return this;
    }

    public BeanManifest update(@NonNull ComponentCreator creator, @NonNull Injector injector) {

        if (this.object == null) {
            Optional<?> injectable = injector.getExact(this.name, this.type);
            if (injectable.isPresent()) {
                this.object = injectable.get();
            } else if (this.ready(creator, injector) && creator.isComponent(this.type) && (this.source != BeanSource.INJECT)) {
                this.object = creator.make(this, injector);
                injector.registerInjectable(this.name, this.object);
            }
        }

        this.invokeInjectDependencies(injector);
        this.invokeMethodDependencies(creator, injector);
        this.updateAllDependencies(creator, injector);
        return this;
    }

    private void updateAllDependencies(@NonNull ComponentCreator creator, @NonNull Injector injector) {
        for (BeanManifest depend : this.depends) {
            depend.update(creator, injector);
        }
    }

    private void invokeMethodDependencies(@NonNull ComponentCreator creator, @NonNull Injector injector) {
        for (BeanManifest depend : this.depends) {

            if ((this.object == null) || !depend.ready(creator, injector) || (depend.getSource() != BeanSource.METHOD) || (depend.getObject() != null)) {
                continue;
            }

            // create object using creator
            Object createdObject = creator.make(depend, injector);

            // a little hack to fool stO0opid BeanManifest - void bean can be executable method eg. @Scheduled
            if ((createdObject == null) && (depend.getMethod().getReturnType() == void.class)) {
                depend.setObject(void.class);
                continue;
            }

            // standard bean
            if (createdObject != null) {
                depend.setObject(createdObject);
                injector.registerInjectable(depend.getName(), depend.getObject());
                continue;
            }

            throw new RuntimeException("Cannot register null as bean [method: " + depend.getMethod() + "]");
        }
    }

    private void invokeInjectDependencies(@NonNull Injector injector) {
        for (BeanManifest depend : this.depends) {
            if ((depend.getSource() != BeanSource.INJECT) || (depend.getObject() != null)) {
                continue;
            }
            Optional<?> injectable = injector.getExact(depend.getName(), depend.getType());
            if (!injectable.isPresent()) {
                continue;
            }
            depend.setObject(injectable.get());
        }
    }

    private void injectExternals(@NonNull Injector injector, @NonNull ExternalResourceProvider resourceProvider) {

        for (External external : this.getExternals()) {
            if (injector.getExact(external.name(), external.type()).isPresent()) {
                continue;
            }
            Object value = resourceProvider.provide(external.name(), external.type(), external.of());
            injector.registerInjectable(external.name(), value);
        }

        for (BeanManifest depend : this.getDepends()) {
            if (depend.getSource() == BeanSource.INJECT) {
                continue;
            }
            depend.injectExternals(injector, resourceProvider);
        }
    }

    public boolean ready(@NonNull ComponentCreator creator, @NonNull Injector injector) {
        return this.ready(creator, injector, true);
    }

    public boolean ready(@NonNull ComponentCreator creator, @NonNull Injector injector, boolean registerFail) {

        for (BeanManifest depend : this.depends) {

            if ((depend.getObject() == null) && (depend.getSource() == BeanSource.INJECT)) {

                Optional<?> injectable = injector.getExact(depend.getName(), depend.getType());

                if (!injectable.isPresent()) {

                    if (!registerFail) {
                        return false;
                    }

                    Class<?> dependClass = depend.getType();
                    DependencyPair dependencyPair = new DependencyPair(depend.getName(), dependClass);
                    int newValue = this.failCounter.getOrDefault(dependencyPair, 0) + 1;
                    this.failCounter.put(dependencyPair, newValue);

                    if (newValue > 100) {
                        this.failCounter.entrySet().stream()
                                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                                .filter(entry -> entry.getValue() > 10)
                                .forEach((entry) -> LOGGER.severe(entry.getKey() + " - " + entry.getValue() + " fails"));
                        throw new RuntimeException("Failed to resolve component/bean " + dependClass + " (" + depend.getName() + "=" + depend.getSource() + ") in " + this.getType() + ":\n"
                                + injector.all().stream().map(i -> "- '" + i.getName() + "' -> " + i.getType()).collect(Collectors.joining("\n")));
                    }

                    return false;
                }

                Object injectObject = injectable.get();
                depend.setObject(injectObject);
            }
        }

        return true;
    }

    private boolean fullLoadReady(@NonNull Injector injector) {
        return this.depends.stream().noneMatch(depend -> depend.getObject() == null);
    }

    public BeanManifest execute(@NonNull ComponentCreator creator, @NonNull Injector injector, @NonNull ExternalResourceProvider resourceProvider) {

        long start = System.currentTimeMillis();
        this.injectExternals(injector, resourceProvider);

        while (!this.ready(creator, injector) || (this.fullLoad && !this.fullLoadReady(injector))) {

            // emergency break
            if ((System.currentTimeMillis() - start) > TimeUnit.SECONDS.toMillis(60)) {
                throw new RuntimeException("#execute() timed out after 60 seconds: \n\n" + this + "\n\n");
            }

            this.update(creator, injector);
        }

        return this;
    }

    public static String nameClass(@NonNull Class<?> clazz) {
        String text = clazz.getSimpleName();
        char[] chars = text.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }
}
