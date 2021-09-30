package eu.okaeri.platform.web.meta;

import eu.okaeri.platform.web.annotation.Controller;
import eu.okaeri.platform.web.meta.role.SimpleRouteRole;
import io.javalin.http.HandlerType;
import lombok.Data;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Map;

@Data
public class RequestHandlerMeta {

    public static RequestHandlerMeta of(Class<?> parentClass, Method method) {

        Annotation[] handlers = RequestHandlerHelper.findHandlers(method);
        if (handlers.length != 1) {
            throw new IllegalArgumentException("Cannot process method with " + handlers.length + " handler annotations: " + method);
        }

        Annotation handlerAnnotation = handlers[0];
        String path = RequestHandlerHelper.readHandlerPath(handlerAnnotation);
        HandlerType type = RequestHandlerHelper.readHandlerType(handlerAnnotation);
        String[] handlerPermittedRoles = RequestHandlerHelper.readPermittedRoles(handlerAnnotation);

        Parameter[] parameters = method.getParameters();
        int[] contextIndexes = RequestHandlerHelper.readContextIndexes(parameters);
        Map<Integer, PathParamMeta> pathParams = PathParamMeta.of(parameters);

        Controller controller = parentClass.getAnnotation(Controller.class);
        String pathPrefix = controller == null ? "" : controller.path();

        String[] defaultPermittedRoles = controller == null ? new String[]{"ANYONE"} : controller.defaultPermittedRoles();
        String[] permittedRoleNames = handlerPermittedRoles.length > 0 ? handlerPermittedRoles : defaultPermittedRoles;
        SimpleRouteRole[] permittedRoles = Arrays.stream(permittedRoleNames).map(SimpleRouteRole::new).toArray(SimpleRouteRole[]::new);

        return new RequestHandlerMeta(method, pathPrefix + path, type, permittedRoles, contextIndexes, pathParams);
    }

    private final Method method;
    private final String path;
    private final HandlerType type;
    private final SimpleRouteRole[] permittedRoles;

    private final int[] contextIndexes;
    private final Map<Integer, PathParamMeta> pathParams;
}
