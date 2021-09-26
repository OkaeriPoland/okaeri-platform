package eu.okaeri.platform.web.component.type;

import eu.okaeri.commons.cache.CacheMap;
import eu.okaeri.injector.Injector;
import eu.okaeri.injector.OkaeriInjector;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.platform.core.component.creator.ComponentCreator;
import eu.okaeri.platform.core.component.creator.ComponentResolver;
import eu.okaeri.platform.core.component.manifest.BeanManifest;
import eu.okaeri.platform.web.meta.PathParamMeta;
import eu.okaeri.platform.web.meta.RequestHandlerHelper;
import eu.okaeri.platform.web.meta.RequestHandlerMeta;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import lombok.NonNull;
import org.slf4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Map;

public class RequestHandlerComponentResolver implements ComponentResolver {

    private static final Map<Integer, Object[]> prefilledCallCache = new CacheMap<>(256);

    @Inject private Javalin javalin;
    @Inject private Logger logger;

    @Override
    public boolean supports(@NonNull Class<?> type) {
        return false;
    }

    @Override
    public boolean supports(@NonNull Method method) {
        return method.getAnnotations().length > 0 && RequestHandlerHelper.findHandlers(method).length > 0;
    }

    @Override
    public Object make(@NonNull ComponentCreator creator, @NonNull BeanManifest manifest, @NonNull Injector injector) {

        BeanManifest parent = manifest.getParent();
        Class<?> parentClass = parent.getType();
        Method method = manifest.getMethod();

        RequestHandlerMeta handlerMeta = RequestHandlerMeta.of(parentClass, method);
        int[] contextIndexes = handlerMeta.getContextIndexes();

        Handler handler = context -> {
            Object[] call = null;
            try {
                call = this.getCall(handlerMeta, context, injector);
                method.invoke(parent.getObject(), call);
                this.flushCall(call, contextIndexes);
            }
            catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException exception) {
                this.logger.error("Handler (" + method + ") failure [" + Arrays.toString(call) + "]", exception);
            }
        };

        this.javalin.addHandler(handlerMeta.getType(), handlerMeta.getPath(), handler, handlerMeta.getPermittedRoles());
        creator.increaseStatistics("handlers", 1);

        return handler;
    }

    private void flushCall(Object[] call, int[] contextIndexes) {
        for (int contextIndex : contextIndexes) {
            call[contextIndex] = null;
        }
    }

    private Object[] getCall(@NonNull RequestHandlerMeta handlerMeta, @NonNull Context context, @NonNull Injector injector) {

        Object[] prefilledCall = prefilledCallCache.computeIfAbsent(Thread.currentThread().hashCode(), (hash) -> {
            OkaeriInjector okaeriInjector = (OkaeriInjector) injector;
            Parameter[] parameters = handlerMeta.getMethod().getParameters();
            return okaeriInjector.fillParameters(parameters, false);
        });

        int[] contextIndexes = handlerMeta.getContextIndexes();
        for (int contextIndex : contextIndexes) {
            prefilledCall[contextIndex] = context;
        }

        Map<Integer, PathParamMeta> pathParams = handlerMeta.getPathParams();
        for (PathParamMeta pathParam : pathParams.values()) {
            Object paramValue = context.pathParamAsClass(pathParam.getName(), pathParam.getType()).get();
            prefilledCall[pathParam.getIndex()] = paramValue;
        }

        return prefilledCall;
    }
}
