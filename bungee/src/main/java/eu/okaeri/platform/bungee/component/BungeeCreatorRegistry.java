package eu.okaeri.platform.bungee.component;

import eu.okaeri.injector.Injector;
import eu.okaeri.platform.bungee.component.type.DelayedComponentResolver;
import eu.okaeri.platform.bungee.component.type.ListenerComponentResolver;
import eu.okaeri.platform.bungee.component.type.MessagesComponentResolver;
import eu.okaeri.platform.bungee.component.type.ScheduledComponentResolver;
import eu.okaeri.platform.core.component.creator.ComponentCreatorRegistry;
import eu.okaeri.platform.core.component.type.BeanComponentResolver;
import eu.okaeri.platform.core.component.type.ConfigurationComponentResolver;
import eu.okaeri.platform.core.component.type.DocumentCollectionComponentResolver;
import eu.okaeri.platform.core.component.type.ServiceDescriptorComponentResolver;

public class BungeeCreatorRegistry extends ComponentCreatorRegistry {

    public BungeeCreatorRegistry(Injector injector) {
        super(injector);
        this.register(BeanComponentResolver.class);
        this.register(ConfigurationComponentResolver.class);
        this.register(DelayedComponentResolver.class);
        this.register(DocumentCollectionComponentResolver.class);
        this.register(ListenerComponentResolver.class);
        this.register(MessagesComponentResolver.class);
        this.register(ServiceDescriptorComponentResolver.class);
        this.register(ScheduledComponentResolver.class);
    }
}
