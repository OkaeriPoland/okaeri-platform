package eu.okaeri.platform.bukkit.component;

import eu.okaeri.platform.bukkit.OkaeriBukkitPlugin;
import eu.okaeri.platform.core.component.creator.ComponentCreator;
import eu.okaeri.platform.core.component.creator.ComponentCreatorRegistry;
import lombok.NonNull;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BukkitComponentCreator extends ComponentCreator {

    private final OkaeriBukkitPlugin plugin;
    private final List<String> logs = Collections.synchronizedList(new ArrayList<>());

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
        if (Bukkit.isPrimaryThread()) {
            Arrays.stream(("- " + message).split("\n"))
                    .forEach(line -> this.plugin.getLogger().info(line));
            return;
        }
        this.logs.add("~ " + message);
    }

    public void dispatchLogs() {
        this.logs.stream()
                .flatMap(asyncLog -> Arrays.stream(asyncLog.split("\n")))
                .forEach(line -> this.plugin.getLogger().info(line));
    }
}
