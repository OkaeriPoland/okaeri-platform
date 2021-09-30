package eu.okaeri.platform.web.meta;

import eu.okaeri.platform.web.annotation.PathParam;
import lombok.Data;

import java.lang.reflect.Parameter;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class PathParamMeta {

    public static PathParamMeta of(int index, Parameter parameter) {
        PathParam pathParam = parameter.getAnnotation(PathParam.class);
        if (pathParam == null) {
            throw new IllegalArgumentException("No PathParam annotation found: " + parameter);
        }
        return new PathParamMeta(index, pathParam.value(), parameter.getType());
    }

    public static Map<Integer, PathParamMeta> of(Parameter[] parameters) {
        Map<Integer, PathParamMeta> metaMap = new LinkedHashMap<>();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (parameter.getAnnotation(PathParam.class) == null) {
                continue;
            }
            PathParamMeta meta = PathParamMeta.of(i, parameter);
            metaMap.put(i, meta);
        }
        return metaMap;
    }

    private final int index;
    private final String name;
    private final Class<?> type;
}
