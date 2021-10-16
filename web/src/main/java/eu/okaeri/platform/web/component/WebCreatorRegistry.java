package eu.okaeri.platform.web.component;

import eu.okaeri.injector.Injector;
import eu.okaeri.platform.standalone.component.ApplicationCreatorRegistry;
import eu.okaeri.platform.web.annotation.PathParam;
import eu.okaeri.platform.web.component.type.RequestHandlerComponentResolver;
import io.javalin.http.Context;

public class WebCreatorRegistry extends ApplicationCreatorRegistry {

    public WebCreatorRegistry(Injector injector) {
        super(injector);
    }

    @Override
    public void register() {
        this.register(RequestHandlerComponentResolver.class);
        this.registerDynamicType(Context.class);
        this.registerDynamicAnnotation(PathParam.class);
    }
}
