package eu.okaeri.platform.bungee.component;

import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.platform.bungee.OkaeriBungeePlugin;
import eu.okaeri.platform.core.component.creator.ComponentCreator;
import eu.okaeri.platform.core.component.creator.ComponentCreatorRegistry;
import lombok.NonNull;

import java.util.Arrays;

public class BungeeComponentCreator extends ComponentCreator {

    private final OkaeriBungeePlugin plugin;

    @Inject
    public BungeeComponentCreator(@NonNull OkaeriBungeePlugin plugin, @NonNull ComponentCreatorRegistry creatorRegistry) {
        super(creatorRegistry);
        this.plugin = plugin;
    }

    @Override
    public boolean isComponent(@NonNull Class<?> type) {
        return OkaeriBungeePlugin.class.isAssignableFrom(type) || super.isComponent(type);
    }

    @Override
    public void log(@NonNull String message) {
        Arrays.stream(message.split("\n")).forEach(line -> this.plugin.log("- " + line));
    }
}
