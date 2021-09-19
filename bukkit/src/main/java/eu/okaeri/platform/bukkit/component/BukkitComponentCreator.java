package eu.okaeri.platform.bukkit.component;

import eu.okaeri.injector.Injector;
import eu.okaeri.platform.bukkit.OkaeriBukkitPlugin;
import eu.okaeri.platform.core.component.ComponentCreator;
import eu.okaeri.platform.core.component.creator.ComponentCreatorRegistry;
import eu.okaeri.platform.core.component.manifest.BeanManifest;
import eu.okaeri.platform.core.component.manifest.BeanSource;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class BukkitComponentCreator implements ComponentCreator {

    private final OkaeriBukkitPlugin plugin;
    private final ComponentCreatorRegistry creatorRegistry;

    private final List<String> asyncLogs = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, Integer> statisticsMap = new TreeMap<>();

    public void dispatchLogs() {
        this.asyncLogs.stream()
                .flatMap(asyncLog -> Arrays.stream(asyncLog.split("\n")))
                .forEach(line -> this.plugin.getLogger().info(line));
    }

    @Override
    public void log(@NonNull String message) {
        if (Bukkit.isPrimaryThread()) {
            Arrays.stream(("- " + message).split("\n"))
                    .forEach(line -> this.plugin.getLogger().info(line));
            return;
        }
        this.asyncLogs.add("~ " + message);
    }

    @Override
    public void increaseStatistics(String identifier, int count) {
        this.statisticsMap.put(identifier, this.statisticsMap.getOrDefault(identifier, 0) + 1);
    }

    @Override
    public boolean isComponent(@NonNull Class<?> type) {
        return OkaeriBukkitPlugin.class.isAssignableFrom(type) || this.creatorRegistry.supports(type);
    }

    @Override
    public boolean isComponentMethod(@NonNull Method method) {
        return this.creatorRegistry.supports(method);
    }

    @Override
    public Object make(@NonNull BeanManifest manifest, @NonNull Injector injector) {

        // validation
        if (!Arrays.asList(BeanSource.METHOD, BeanSource.COMPONENT).contains(manifest.getSource())) {
            throw new RuntimeException("Cannot transform from source " + manifest.getSource());
        }

        // use CreatorRegistry to create components
        Optional<Object> objectOptional = this.creatorRegistry.make(this, manifest);

        // returned value does not seem right
        if (!objectOptional.isPresent()) {
            throw new IllegalArgumentException("Found unresolvable component: " + manifest);
        }

        // extract ready object
        return objectOptional.get();
    }

    public String getSummaryText(long took) {
        String statistics = this.statisticsMap.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining(", "));
        return "= (" + statistics + ") [blocking: " + took + " ms]";
    }
}
