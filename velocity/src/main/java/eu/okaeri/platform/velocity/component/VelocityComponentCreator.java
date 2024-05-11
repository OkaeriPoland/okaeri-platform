package eu.okaeri.platform.velocity.component;

import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.platform.velocity.OkaeriVelocityPlugin;
import eu.okaeri.platform.core.component.creator.ComponentCreator;
import eu.okaeri.platform.core.component.creator.ComponentCreatorRegistry;
import lombok.NonNull;

import java.util.Arrays;

public class VelocityComponentCreator extends ComponentCreator {

    private final OkaeriVelocityPlugin plugin;

    @Inject
    public VelocityComponentCreator(@NonNull OkaeriVelocityPlugin plugin, @NonNull ComponentCreatorRegistry creatorRegistry) {
        super(creatorRegistry);
        this.plugin = plugin;
    }

    @Override
    public boolean isComponent(@NonNull Class<?> type) {
        return OkaeriVelocityPlugin.class.isAssignableFrom(type) || super.isComponent(type);
    }

    @Override
    public void log(@NonNull String message) {
        Arrays.stream(message.split("\n")).forEach(line -> this.plugin.log("- " + line));
    }
}
