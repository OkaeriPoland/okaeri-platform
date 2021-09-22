package eu.okaeri.platform.bungee.component;

import eu.okaeri.platform.bungee.OkaeriBungeePlugin;
import eu.okaeri.platform.core.component.creator.ComponentCreator;
import eu.okaeri.platform.core.component.creator.ComponentCreatorRegistry;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BungeeComponentCreator extends ComponentCreator {

    private final OkaeriBungeePlugin plugin;
    private final List<String> logs = Collections.synchronizedList(new ArrayList<>());

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
        this.logs.add(message);
    }

    public void dispatchLogs(String prefix) {
        this.logs.stream()
                .flatMap(asyncLog -> Arrays.stream(asyncLog.split("\n")))
                .forEach(line -> this.plugin.getLogger().info(prefix + " " + line));
        this.logs.clear();
    }
}
