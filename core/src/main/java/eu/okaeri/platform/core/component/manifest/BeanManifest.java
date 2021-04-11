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
        manifest.setType(parameter.getType());
        manifest.setName(name);
        manifest.setDepends(Collections.emptyList());
        manifest.setSource(BeanSource.INJECT);

        return manifest;
    }

    private static BeanManifest of(Field field) {

        Inject inject = field.getAnnotation(Inject.class);
        String name = (inject == null) ? "" : inject.value();

        BeanManifest manifest = new BeanManifest();
        manifest.setName(name);
        manifest.setType(field.getType());
        manifest.setDepends(Collections.emptyList());
        manifest.setSource(BeanSource.INJECT);

        return manifest;
    }

    public static BeanManifest of(Class<?> clazz, ComponentCreator creator, boolean fullLoad) {

        if (!creator.isComponent(clazz)) {
            throw new IllegalArgumentException("Cannot create manifest of non-component " + clazz);
        }

        BeanManifest manifest = new BeanManifest();
        manifest.setType(clazz);
        manifest.setName("");
        manifest.setSource(BeanSource.COMPONENT);
        manifest.setFullLoad(fullLoad);

        List<BeanManifest> depends = new ArrayList<>();
        manifest.setDepends(depends);

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
                .filter(creator::isComponentMethod)
                .map(method -> BeanManifest.of(method, creator))
                .collect(Collectors.toList()));

        return manifest;
    }

    public static BeanManifest of(Method method, ComponentCreator creator) {

        Bean annotation = method.getAnnotation(Bean.class);
        if ((annotation == null) && !creator.isComponentMethod(method)) {
            throw new IllegalArgumentException("cannot create BeanManifest from method without @Bean: " + method);
        }

        BeanManifest manifest = new BeanManifest();
        manifest.setType(method.getReturnType());
        manifest.setName((annotation == null) ? "" : annotation.value());
        manifest.setDepends(Arrays.stream(method.getParameters())
                .map(BeanManifest::of)
                .collect(Collectors.toList()));
        manifest.setSource(BeanSource.METHOD);
        manifest.setMethod(method);
        manifest.setRegister((annotation == null) || annotation.register());

        return manifest;
    }

    public static BeanManifest of(Register register, ComponentCreator creator) {

        BeanManifest manifest = of(register.value(), creator, false);
        manifest.setRegister(register.register());

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
            } else if (this.ready(injector) && creator.isComponent(this.type) && (this.source != BeanSource.INJECT)) {
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

            if ((this.object == null) || !depend.ready(injector) || (depend.getSource() != BeanSource.METHOD) || (depend.getObject() != null)) {
                continue;
            }

            depend.setParent(this.object);
            Object createdObject = creator.makeObject(depend, injector);

            // a little hack to fool stO0opid BeanManifest - void bean can be executable method eg. @Timer
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

    private void invokeInjectDependencies(Injector injector) {
        for (BeanManifest depend : this.depends) {
            if ((depend.getSource() != BeanSource.INJECT) || (depend.getObject() != null)) {
                continue;
            }
            Optional<? extends Injectable<?>> injectable = injector.getInjectable(depend.getName(), depend.getType());
            if (!injectable.isPresent()) {
                continue;
            }
            depend.setObject(injectable.get().getObject());
        }
    }

    public boolean ready(Injector injector) {

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
        return this.depends.stream().noneMatch(depend -> depend.getObject() == null);
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
