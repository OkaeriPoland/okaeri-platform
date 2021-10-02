package eu.okaeri.platform.core.component;

import eu.okaeri.injector.Injectable;
import eu.okaeri.injector.Injector;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.injector.annotation.PostConstruct;
import eu.okaeri.platform.core.component.manifest.BeanManifest;
import eu.okaeri.platform.core.component.manifest.BeanSource;
import eu.okaeri.platform.core.exception.BreakException;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.ToString;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

public final class ComponentHelper {

    public static Object invokeMethod(@NonNull BeanManifest manifest, @NonNull Injector injector) {
        if (manifest.getParent().getObject() == null) {
            throw new IllegalArgumentException("manifest.parent.object cannot be null to invoke method: " + manifest);
        }
        return invokeMethod(manifest.getParent().getObject(), manifest.getMethod(), injector);
    }

    public static Object invokeMethod(@NonNull Object object, @NonNull Method method, @NonNull Injector injector) {

        // gain access *hackerman*
        method.setAccessible(true);

        // check for injectable parameters
        Parameter[] parameters = method.getParameters();
        Object[] call = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {

            Parameter param = parameters[i];
            Class<?> paramType = param.getType();
            String name = (param.getAnnotation(Inject.class) != null) ? param.getAnnotation(Inject.class).value() : "";

            Optional<? extends Injectable<?>> injectable = injector.getInjectable(name, paramType);
            if (!injectable.isPresent()) {
                throw new RuntimeException("Cannot invoke " + displayMethod(method) + ", no injectable of type " + paramType + " [" + name + "] found");
            }

            call[i] = paramType.cast(injectable.get().getObject());
        }

        // let's make a call :dab:
        Object result;
        try {
            result = method.invoke(object, call);
        } catch (Exception exception) {
            if (exception instanceof InvocationTargetException) {
                if (exception.getCause() instanceof BreakException) {
                    throw (BreakException) exception.getCause();
                }
                throw new RuntimeException("Error invoking " + displayMethod(method), exception.getCause());
            }
            throw new RuntimeException("Error invoking " + displayMethod(method), exception);
        }

        return result;
    }

    @SneakyThrows
    public static void injectComponentFields(Object component, @NonNull Injector injector) {

        if (component == null) return;
        Class<?> beanClazz = component.getClass();
        Field[] fields = beanClazz.getDeclaredFields();

        for (Field field : fields) {

            Inject inject = field.getAnnotation(Inject.class);
            if (inject == null) {
                continue;
            }

            Optional<? extends Injectable<?>> injectable = injector.getInjectable(inject.value(), field.getType());
            if (!injectable.isPresent()) {
                continue;
            }

            Injectable<?> injectObject = injectable.get();
            field.setAccessible(true);
            field.set(component, injectObject.getObject());
        }
    }

    public static String displayMethod(@NonNull Method method) {
        return method.getReturnType().getSimpleName() + "->" + method.getName()
                + "(" + Arrays.stream(method.getParameters())
                .map(parameter -> parameter.getType().getSimpleName())
                .collect(Collectors.joining(", ")) + ")";
    }

    public static void invokePostConstruct(Object object, @NonNull Injector injector) {

        if (object == null) {
            return;
        }

        List<Method> postConstructs = Arrays.stream(object.getClass().getDeclaredMethods())
                .filter(method -> method.getAnnotation(PostConstruct.class) != null)
                .sorted(Comparator.comparingInt(method -> method.getAnnotation(PostConstruct.class).order()))
                .collect(Collectors.toList());

        for (Method postConstruct : postConstructs) {

            Object result = ComponentHelper.invokeMethod(object, postConstruct, injector);
            if (result == null) {
                continue;
            }

            injector.registerInjectable(postConstruct.getName(), result);
        }
    }

    public static void closeAllOfType(@NonNull Class<? extends Closeable> type, @NonNull Injector injector) {
        injector.allOf(type).stream()
                .map(Injectable::getObject)
                .forEach(closeable -> {
                    try {
                        closeable.close();
                    } catch (IOException ignored) {
                    }
                });
    }

    public static ComponentMessageBuilder buildComponentMessage() {
        return new ComponentMessageBuilder();
    }

    public static File getJarFile(Class<?> clazz) {
        try {
            return new File(clazz.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException exception) {
            throw new RuntimeException("Failed to resolve jar file of " + clazz, exception);
        }
    }

    public static Runnable manifestToRunnable(BeanManifest manifest, Injector injector) {
        Runnable runnable;
        if (manifest.getSource() == BeanSource.METHOD) {
            if (Runnable.class.isAssignableFrom(manifest.getType())) {
                runnable = (Runnable) invokeMethod(manifest, injector);
            } else if (manifest.getType() == void.class) {
                runnable = () -> invokeMethod(manifest, injector);
            } else {
                throw new IllegalArgumentException("Scheduled/Delayed method should return java.lang.Runnable or void: " + manifest);
            }
            manifest.setName(manifest.getMethod().getName());
        } else {
            if (!Runnable.class.isAssignableFrom(manifest.getType())) {
                throw new IllegalArgumentException("Scheduled/Delayed component requires class to be a java.lang.Runnable: " + manifest);
            }
            runnable = (Runnable) injector.createInstance(manifest.getType());
        }
        return runnable;
    }

    @ToString
    public static class ComponentMessageBuilder {

        private String type;
        private String name;
        private Long took;

        private final Map<String, Object> meta = new TreeMap<>();
        private final List<String> footer = new ArrayList<>();

        public ComponentMessageBuilder type(String type) {
            this.type = type;
            return this;
        }

        public ComponentMessageBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ComponentMessageBuilder meta(String name, Object value) {
            this.meta.put(name, value);
            return this;
        }

        public ComponentMessageBuilder took(long took) {
            this.took = took;
            return this;
        }

        public String build() {

            if (this.type == null) {
                throw new IllegalArgumentException("type cannot be null");
            }

            StringBuilder metaBuilder = new StringBuilder();
            metaBuilder.append(this.type);

            if (this.name == null) {
                throw new IllegalArgumentException("name cannot be null");
            }

            metaBuilder.append(": ");
            metaBuilder.append(this.name);

            if (!this.meta.isEmpty()) {
                metaBuilder.append(" { ");
                metaBuilder.append(this.meta.entrySet().stream()
                        .map(entry -> {
                            Object rendered = entry.getValue();
                            if (rendered instanceof String) {
                                rendered = "'" + rendered + "'";
                            }
                            return entry.getKey() + " = " + rendered;
                        })
                        .collect(Collectors.joining(", ")));
                metaBuilder.append(" }");
            }

            if (this.took != null) {
                metaBuilder.append(" [");
                metaBuilder.append(this.took);
                metaBuilder.append(" ms]");
            }

            if (!this.footer.isEmpty()) {
                for (String line : this.footer) {
                    metaBuilder.append("\n");
                    metaBuilder.append(line);
                }
            }

            return metaBuilder.toString();
        }

        public ComponentMessageBuilder footer(String line) {
            this.footer.add(line);
            return this;
        }
    }
}
