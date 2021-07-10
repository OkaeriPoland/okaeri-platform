package eu.okaeri.platform.core.component;

import eu.okaeri.injector.Injectable;
import eu.okaeri.injector.Injector;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.platform.core.component.manifest.BeanManifest;
import eu.okaeri.platform.core.exception.BreakException;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public final class ComponentHelper {

    public static Object invokeMethod(@NonNull BeanManifest manifest, @NonNull Injector injector) {
        return invokeMethod(manifest.getParent(), manifest.getMethod(), injector);
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
        return  method.getReturnType().getSimpleName() + "->" + method.getName()
                + "(" + Arrays.stream(method.getParameters())
                .map(parameter -> parameter.getType().getSimpleName())
                .collect(Collectors.joining(", ")) + ")";
    }
}
