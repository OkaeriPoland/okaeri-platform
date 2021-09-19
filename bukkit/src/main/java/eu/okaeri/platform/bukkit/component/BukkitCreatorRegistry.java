package eu.okaeri.platform.bukkit.component;

import eu.okaeri.injector.Injector;
import eu.okaeri.platform.bukkit.component.type.*;
import eu.okaeri.platform.core.component.creator.ComponentCreatorRegistry;
import eu.okaeri.platform.core.component.type.BeanComponentResolver;
import eu.okaeri.platform.core.component.type.ConfigurationComponentResolver;
import eu.okaeri.platform.core.component.type.DocumentCollectionComponentResolver;
import eu.okaeri.platform.core.component.type.ServiceDescriptorComponentResolver;

public class BukkitCreatorRegistry extends ComponentCreatorRegistry {

    public BukkitCreatorRegistry(Injector injector) {
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
