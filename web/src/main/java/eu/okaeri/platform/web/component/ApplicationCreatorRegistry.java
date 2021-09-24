package eu.okaeri.platform.web.component;

import eu.okaeri.injector.Injector;
import eu.okaeri.platform.core.component.creator.ComponentCreatorRegistry;
import eu.okaeri.platform.core.component.type.*;
import eu.okaeri.platform.standalone.component.type.MessagesComponentResolver;
import eu.okaeri.platform.web.component.type.HandlerComponentResolver;
import io.javalin.http.Context;

public class ApplicationCreatorRegistry extends ComponentCreatorRegistry {

    public ApplicationCreatorRegistry(Injector injector) {
        super(injector);
        // custom first
        this.register(ConfigurationComponentResolver.class);
        this.register(DocumentCollectionComponentResolver.class);
        this.register(MessagesComponentResolver.class);
        this.register(ServiceDescriptorComponentResolver.class);
        // web
        this.register(HandlerComponentResolver.class);
        this.registerDynamicType(Context.class);
        // generic last
        this.register(BeanComponentResolver.class);
        this.register(GenericComponentResolver.class);
    }
}
