package eu.okaeri.platform.web.meta;

import eu.okaeri.platform.web.annotation.*;
import io.javalin.http.Context;
import io.javalin.http.HandlerType;
import lombok.NonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public final class RequestHandlerHelper {

    public static int[] readContextIndexes(@NonNull Parameter[] parameters) {

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

    public static Annotation[] findHandlers(@NonNull Method method) {
        return Stream.of(AfterHandler.class, BeforeHandler.class, DeleteHandler.class, GetHandler.class, HeadHandler.class,
                        PatchHandler.class, PostHandler.class, PutHandler.class, RequestHandler.class)
                .map(method::getAnnotation)
                .filter(Objects::nonNull)
                .toArray(Annotation[]::new);
    }

    public static String readHandlerPath(@NonNull Annotation annotation) {
        if (annotation instanceof AfterHandler) {
            return ((AfterHandler) annotation).path();
        } else if (annotation instanceof BeforeHandler) {
            return ((BeforeHandler) annotation).path();
        } else if (annotation instanceof DeleteHandler) {
            return ((DeleteHandler) annotation).path();
        } else if (annotation instanceof GetHandler) {
            return ((GetHandler) annotation).path();
        } else if (annotation instanceof HeadHandler) {
            return ((HeadHandler) annotation).path();
        } else if (annotation instanceof PatchHandler) {
            return ((PatchHandler) annotation).path();
        } else if (annotation instanceof PostHandler) {
            return ((PostHandler) annotation).path();
        } else if (annotation instanceof PutHandler) {
            return ((PutHandler) annotation).path();
        } else if (annotation instanceof RequestHandler) {
            return ((RequestHandler) annotation).path();
        }
        throw new IllegalArgumentException("Unsupported handler annotation: " + annotation);
    }

    public static HandlerType readHandlerType(@NonNull Annotation annotation) {
        if (annotation instanceof AfterHandler) {
            return HandlerType.AFTER;
        } else if (annotation instanceof BeforeHandler) {
            return HandlerType.BEFORE;
        } else if (annotation instanceof DeleteHandler) {
            return HandlerType.DELETE;
        } else if (annotation instanceof GetHandler) {
            return HandlerType.GET;
        } else if (annotation instanceof HeadHandler) {
            return HandlerType.HEAD;
        } else if (annotation instanceof PatchHandler) {
            return HandlerType.PATCH;
        } else if (annotation instanceof PostHandler) {
            return HandlerType.POST;
        } else if (annotation instanceof PutHandler) {
            return HandlerType.PUT;
        } else if (annotation instanceof RequestHandler) {
            return ((RequestHandler) annotation).type();
        }
        throw new IllegalArgumentException("Unsupported handler annotation: " + annotation);
    }

    public static String[] readPermittedRoles(@NonNull Annotation annotation) {
        if (annotation instanceof AfterHandler) {
            return ((AfterHandler) annotation).permittedRoles();
        } else if (annotation instanceof BeforeHandler) {
            return ((BeforeHandler) annotation).permittedRoles();
        } else if (annotation instanceof DeleteHandler) {
            return ((DeleteHandler) annotation).permittedRoles();
        } else if (annotation instanceof GetHandler) {
            return ((GetHandler) annotation).permittedRoles();
        } else if (annotation instanceof HeadHandler) {
            return ((HeadHandler) annotation).permittedRoles();
        } else if (annotation instanceof PatchHandler) {
            return ((PatchHandler) annotation).permittedRoles();
        } else if (annotation instanceof PostHandler) {
            return ((PostHandler) annotation).permittedRoles();
        } else if (annotation instanceof PutHandler) {
            return ((PutHandler) annotation).permittedRoles();
        } else if (annotation instanceof RequestHandler) {
            return ((RequestHandler) annotation).permittedRoles();
        }
        throw new IllegalArgumentException("Unsupported handler annotation: " + annotation);
    }
}
