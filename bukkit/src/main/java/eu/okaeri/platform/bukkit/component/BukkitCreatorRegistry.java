package eu.okaeri.platform.bukkit.component;

import eu.okaeri.injector.Injector;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.platform.bukkit.component.type.DelayedComponentResolver;
import eu.okaeri.platform.bukkit.component.type.ListenerComponentResolver;
import eu.okaeri.platform.bukkit.component.type.MessagesComponentResolver;
import eu.okaeri.platform.bukkit.component.type.ScheduledComponentResolver;
import eu.okaeri.platform.core.component.creator.ComponentCreatorRegistry;
import eu.okaeri.platform.core.component.type.*;

public class BukkitCreatorRegistry extends ComponentCreatorRegistry {

    @Inject
    public BukkitCreatorRegistry(Injector injector) {
        super(injector);
        // custom first
        this.register(ConfigurationComponentResolver.class);
        this.register(DelayedComponentResolver.class);
        this.register(DocumentCollectionComponentResolver.class);
        this.register(ListenerComponentResolver.class);
        this.register(MessagesComponentResolver.class);
        this.register(CommandComponentResolver.class);
        this.register(ScheduledComponentResolver.class);
        // generic last
        this.register(BeanComponentResolver.class);
        this.register(GenericComponentResolver.class);
    }
}
