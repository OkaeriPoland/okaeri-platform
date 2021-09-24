package eu.okaeri.platform.cli.component;

import eu.okaeri.injector.Injector;
import eu.okaeri.platform.core.component.creator.ComponentCreatorRegistry;
import eu.okaeri.platform.core.component.type.*;
import eu.okaeri.platform.standalone.component.type.MessagesComponentResolver;

public class ApplicationCreatorRegistry extends ComponentCreatorRegistry {

    public ApplicationCreatorRegistry(Injector injector) {
        super(injector);
        // custom first
        this.register(ConfigurationComponentResolver.class);
        this.register(DocumentCollectionComponentResolver.class);
        this.register(MessagesComponentResolver.class);
        this.register(ServiceDescriptorComponentResolver.class);
        // generic last
        this.register(BeanComponentResolver.class);
        this.register(GenericComponentResolver.class);
    }
}
