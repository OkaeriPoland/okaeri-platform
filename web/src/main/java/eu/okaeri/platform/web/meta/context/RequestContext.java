package eu.okaeri.platform.web.meta.context;

import io.javalin.http.Context;
import io.javalin.http.HttpCode;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class RequestContext extends Context {

    public static RequestContext of(Context context) {
        return new RequestContext(context.req, context.res, context.attributeMap());
    }

    public RequestContext(@NotNull HttpServletRequest req, @NotNull HttpServletResponse res, @NotNull Map<String, ?> appAttributes) {
        super(req, res, appAttributes);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public RequestContext jsonOpt(Optional<?> objectOptional) {

        if (objectOptional.isPresent()) {
            this.json(objectOptional.get());
            return this;
        }

        this.status(HttpCode.NOT_FOUND);
        this.json(Collections.singletonMap("error", HttpCode.NOT_FOUND));
        return this;
    }
}
