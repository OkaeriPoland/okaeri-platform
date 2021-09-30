package eu.okaeri.platform.bukkit.component;

import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.platform.bukkit.OkaeriBukkitPlugin;
import eu.okaeri.platform.core.component.creator.ComponentCreator;
import eu.okaeri.platform.core.component.creator.ComponentCreatorRegistry;
import lombok.NonNull;

import java.util.Arrays;

public class BukkitComponentCreator extends ComponentCreator {

    private final OkaeriBukkitPlugin plugin;

    @Inject
    public BukkitComponentCreator(@NonNull OkaeriBukkitPlugin plugin, @NonNull ComponentCreatorRegistry creatorRegistry) {
        super(creatorRegistry);
        this.plugin = plugin;
    }

    @Override
    public boolean isComponent(@NonNull Class<?> type) {
        return OkaeriBukkitPlugin.class.isAssignableFrom(type) || super.isComponent(type);
    }

    @Override
    public void log(@NonNull String message) {
        Arrays.stream(message.split("\n")).forEach(line -> this.plugin.log("- " + line));
    }
}
