package eu.okaeri.platform.core.component.manifest;

import eu.okaeri.injector.Injectable;
import eu.okaeri.injector.Injector;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.platform.core.annotation.Bean;
import eu.okaeri.platform.core.annotation.Register;
import eu.okaeri.platform.core.component.ComponentCreator;
import lombok.Data;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Data
public class BeanManifest {

    public static BeanManifest of(Parameter parameter) {

        Inject inject = parameter.getAnnotation(Inject.class);
        String name = (inject == null) ? "" : inject.value();

        BeanManifest manifest = new BeanManifest();
        manifest.type = parameter.getType();
        manifest.name = name;
        manifest.depends = Collections.emptyList();
        manifest.source = BeanSource.INJECT;

        return manifest;
    }

    private static BeanManifest of(Field field) {

        Inject inject = field.getAnnotation(Inject.class);
        String name = (inject == null) ? "" : inject.value();

        BeanManifest manifest = new BeanManifest();
        manifest.name = name;
        manifest.type = field.getType();
        manifest.depends = Collections.emptyList();
        manifest.source = BeanSource.INJECT;

        return manifest;
    }

    public static BeanManifest of(Class<?> clazz, ComponentCreator creator, boolean fullLoad) {

        if (!creator.isComponent(clazz)) {
            throw new IllegalArgumentException("Cannot create manifest of non-component " + clazz);
        }

        BeanManifest manifest = new BeanManifest();
        manifest.type = clazz;
        manifest.name = "";
        manifest.source = BeanSource.COMPONENT;
        manifest.fullLoad = fullLoad;

        List<BeanManifest> depends = new ArrayList<>();
        manifest.depends = depends;

        depends.addAll(Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> field.getAnnotation(Inject.class) != null)
                .map(BeanManifest::of)
                .collect(Collectors.toList()));

        List<Register> registers = new ArrayList<>();
        registers.add(clazz.getAnnotation(Register.class));
        Register.List annotationList = clazz.getAnnotation(Register.List.class);
        registers.addAll((annotationList == null) ? Collections.emptyList() : Arrays.asList(annotationList.value()));

        depends.addAll(registers.stream()
                .filter(Objects::nonNull)
                .map(reg -> BeanManifest.of(reg, creator))
                .collect(Collectors.toList()));

        depends.addAll(Arrays.stream(clazz.getDeclaredMethods())
                .filter(method -> method.getAnnotation(Bean.class) != null)
                .map(BeanManifest::of)
                .collect(Collectors.toList()));

        return manifest;
    }

    public static BeanManifest of(Method method) {

        Bean annotation = method.getAnnotation(Bean.class);
        if (annotation == null) {
            throw new IllegalArgumentException("cannot create BeanManifest from method without @Bean: " + method);
        }

        BeanManifest manifest = new BeanManifest();
        manifest.type = method.getReturnType();
        manifest.name = annotation.value();
        manifest.depends = Arrays.stream(method.getParameters())
                .map(BeanManifest::of)
                .collect(Collectors.toList());
        manifest.source = BeanSource.METHOD;
        manifest.method = method;
        manifest.register = annotation.register();

        return manifest;
    }

    public static BeanManifest of(Register register, ComponentCreator creator) {

        BeanManifest manifest = of(register.value(), creator, false);
        manifest.register = register.register();

        return manifest;
    }

    private String name;
    private Object object;
    private Class<?> type;
    private BeanSource source;
    private Object parent;
    private Method method;
    private boolean register = true;
    private boolean fullLoad = false;
    private List<BeanManifest> depends;
    private Map<Class<?>, Integer> failCounter = new HashMap<>();

    public BeanManifest update(ComponentCreator creator, Injector injector) {

        if (this.object == null) {
            Optional<? extends Injectable<?>> injectable = injector.getInjectable(this.name, this.type);
            if (injectable.isPresent()) {
                this.object = injectable.get().getObject();
            }
            else if (this.ready(injector) && creator.isComponent(this.type) && (this.source != BeanSource.INJECT)) {
                this.object = creator.makeObject(this, injector);
                injector.registerInjectable(this.name, this.object);
            }
        }

        this.invokeInjectDependencies(injector);
        this.invokeMethodDependencies(creator, injector);
        this.updateAllDependencies(creator, injector);
        return this;
    }

    private void updateAllDependencies(ComponentCreator creator, Injector injector) {
        for (BeanManifest depend : this.depends) {
            depend.update(creator, injector);
        }
    }

    private void invokeMethodDependencies(ComponentCreator creator, Injector injector) {
        for (BeanManifest depend : this.depends) {
            if ((this.object == null) || !depend.ready(injector) || (depend.getSource() != BeanSource.METHOD)) {
                continue;
            }
            depend.parent = this.object;
            depend.object = creator.makeObject(depend, injector);
            injector.registerInjectable(depend.name, depend.object);
        }
    }

    private void invokeInjectDependencies(Injector injector) {
        for (BeanManifest depend : this.depends) {
            if (depend.getSource() != BeanSource.INJECT) {
                continue;
            }
            Optional<? extends Injectable<?>> injectable = injector.getInjectable(depend.name, depend.type);
            if (!injectable.isPresent()) {
                continue;
            }
            depend.setObject(injectable.get().getObject());
        }
    }

    private boolean ready(Injector injector) {

        for (BeanManifest depend : this.depends) {

            if ((depend.getObject() == null) && (depend.getSource() == BeanSource.INJECT)) {

                Optional<? extends Injectable<?>> injectable = injector.getInjectable(depend.getName(), depend.getType());

                if (!injectable.isPresent()) {

                    Class<?> dependClass = depend.getType();
                    int newValue = this.failCounter.getOrDefault(dependClass, 0) + 1;
                    this.failCounter.put(dependClass, newValue);

                    if (newValue > 100) {
                        throw new RuntimeException("Failed to resolve component/bean " + dependClass + " (" + depend.getName() + "=" + depend.getSource() + ")");
                    }

                    return false;
                }

                Object injectObject = injectable.get().getObject();
                depend.setObject(injectObject);
            }
        }

        return true;
    }

    private boolean fullLoadReady(Injector injector) {

        for (BeanManifest depend : this.depends) {

            if (depend.getSource() != BeanSource.COMPONENT) {
                continue;
            }

            if (depend.getObject() != null) {
                continue;
            }

            return false;
        }

        return true;
    }

    public BeanManifest execute(ComponentCreator creator, Injector injector) {

        long start = System.currentTimeMillis();

        while (!this.ready(injector) || (this.fullLoad && !this.fullLoadReady(injector))) {

            // emergency break
            if ((System.currentTimeMillis() - start) > TimeUnit.SECONDS.toMillis(60)) {
                throw new RuntimeException("#execute() timed out after 60 seconds: \n\n" + this + "\n\n");
            }

            this.update(creator, injector);
        }

        return this;
    }
}
