package eu.okaeri.platform.standalone.component;

import eu.okaeri.injector.Injector;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.platform.core.component.creator.ComponentCreatorRegistry;
import eu.okaeri.platform.core.component.type.*;
import eu.okaeri.platform.standalone.component.type.MessagesComponentResolver;

public class ApplicationCreatorRegistry extends ComponentCreatorRegistry {

    @Inject
    public ApplicationCreatorRegistry(Injector injector) {
        super(injector);
        // custom first
        this.register(ConfigurationComponentResolver.class);
        this.register(DocumentCollectionComponentResolver.class);
        this.register(MessagesComponentResolver.class);
        this.register(CommandComponentResolver.class);
        // call additional
        this.register();
        // generic last
        this.register(BeanComponentResolver.class);
        this.register(GenericComponentResolver.class);
    }

    public void register() {
    }
}
