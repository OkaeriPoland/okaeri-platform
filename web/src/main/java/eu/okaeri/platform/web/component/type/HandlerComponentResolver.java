package eu.okaeri.platform.web.component.type;

import eu.okaeri.commons.cache.CacheMap;
import eu.okaeri.injector.Injector;
import eu.okaeri.injector.OkaeriInjector;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.platform.core.component.creator.ComponentCreator;
import eu.okaeri.platform.core.component.creator.ComponentResolver;
import eu.okaeri.platform.core.component.manifest.BeanManifest;
import eu.okaeri.platform.web.annotation.Handler;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HandlerType;
import lombok.NonNull;
import org.slf4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HandlerComponentResolver implements ComponentResolver {

    private static final Map<Integer, Object[]> prefilledCallCache = new CacheMap<>(256);

    @Inject private Javalin javalin;
    @Inject private Logger logger;

    @Override
    public boolean supports(@NonNull Class<?> type) {
        return false;
    }

    @Override
    public boolean supports(@NonNull Method method) {
        return method.getAnnotation(Handler.class) != null;
    }

    @Override
    public Object make(@NonNull ComponentCreator creator, @NonNull BeanManifest manifest, @NonNull Injector injector) {

        Method method = manifest.getMethod();
        Handler handler = method.getAnnotation(Handler.class);
        String path = handler.path();
        HandlerType type = handler.type();
        BeanManifest parent = manifest.getParent();

        Parameter[] parameters = method.getParameters();
        int[] contextIndexes = this.readContextIndexes(parameters);

        io.javalin.http.Handler javalinHandler = context -> {
            try {
                Object[] call = this.getCall(contextIndexes, parameters, context, injector);
                method.invoke(parent.getObject(), call);
                this.flushCall(call, contextIndexes);
            }
            catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException exception) {
                this.logger.error("Handler failure", exception);
            }
        };

        this.javalin.addHandler(type, path, javalinHandler);
        creator.increaseStatistics("handlers", 1);

        return javalinHandler;
    }

    private void flushCall(Object[] call, int[] contextIndexes) {
        for (int contextIndex : contextIndexes) {
            call[contextIndex] = null;
        }
    }

    private Object[] getCall(int[] contextIndexes, @NonNull Parameter[] parameters, @NonNull Context context, @NonNull Injector injector) {

        Object[] prefilledCall = prefilledCallCache.computeIfAbsent(Thread.currentThread().hashCode(), (hash) -> {
            OkaeriInjector okaeriInjector = (OkaeriInjector) injector;
            return okaeriInjector.fillParameters(parameters, false);
        });

        for (int contextIndex : contextIndexes) {
            prefilledCall[contextIndex] = context;
        }

        return prefilledCall;
    }

    private int[] readContextIndexes(@NonNull Parameter[] parameters) {

        List<Integer> indexes = new ArrayList<>();

        for (int i = 0; i < parameters.length; i++) {

            Parameter param = parameters[i];
            if (!Context.class.isAssignableFrom(param.getType())) {
                continue;
            }

            indexes.add(i);
        }

        return indexes.stream().mapToInt(Integer::intValue).toArray();
    }
}
