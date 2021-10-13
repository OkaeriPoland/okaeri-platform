package eu.okaeri.platform.web.component;

import eu.okaeri.injector.Injector;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.platform.core.component.creator.ComponentCreatorRegistry;
import eu.okaeri.platform.core.component.type.*;
import eu.okaeri.platform.standalone.component.type.MessagesComponentResolver;
import eu.okaeri.platform.web.annotation.PathParam;
import eu.okaeri.platform.web.component.type.RequestHandlerComponentResolver;
import io.javalin.http.Context;

public class ApplicationCreatorRegistry extends ComponentCreatorRegistry {

    @Inject
    public ApplicationCreatorRegistry(Injector injector) {
        super(injector);
        // custom first
        this.register(ConfigurationComponentResolver.class);
        this.register(DocumentCollectionComponentResolver.class);
        this.register(MessagesComponentResolver.class);
        this.register(CommandComponentResolver.class);
        // web
        this.register(RequestHandlerComponentResolver.class);
        this.registerDynamicType(Context.class);
        this.registerDynamicAnnotation(PathParam.class);
        // generic last
        this.register(BeanComponentResolver.class);
        this.register(GenericComponentResolver.class);
    }
}
