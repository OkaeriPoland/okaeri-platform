package eu.okaeri.platform.core.component.creator;

import eu.okaeri.injector.Injector;
import eu.okaeri.platform.core.component.manifest.BeanManifest;
import eu.okaeri.platform.core.component.manifest.BeanSource;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public abstract class ComponentCreator {

    private static final Set<BeanSource> SUPPORTED_BEAN_SOURCES = new HashSet<>(Arrays.asList(BeanSource.METHOD, BeanSource.COMPONENT));

    private @Getter @NonNull final ComponentCreatorRegistry registry;
    private @Getter(AccessLevel.PROTECTED) @NonNull final Map<String, Integer> statisticsMap = new TreeMap<>();

    public void increaseStatistics(String identifier, int count) {
        this.getStatisticsMap().put(identifier, this.getStatisticsMap().getOrDefault(identifier, 0) + 1);
    }

    public boolean isComponent(@NonNull Class<?> type) {
        return this.getRegistry().supports(type);
    }

    public boolean isComponentMethod(@NonNull Method method) {
        return this.getRegistry().supports(method);
    }

    public Object make(@NonNull BeanManifest manifest, @NonNull Injector injector) {

        // validation
        if (!SUPPORTED_BEAN_SOURCES.contains(manifest.getSource())) {
            throw new RuntimeException("Cannot transform from source " + manifest.getSource());
        }

        // use CreatorRegistry to create components
        Optional<Object> objectOptional = this.getRegistry().make(this, manifest);

        // returned value does not seem right
        if (!objectOptional.isPresent()) {
            throw new IllegalArgumentException("Found unresolvable component: " + manifest);
        }

        // extract ready object
        return objectOptional.get();
    }

    public String getSummaryText(long took) {
        String statistics = this.getStatisticsMap().entrySet().stream()
            .map(entry -> entry.getKey() + ": " + entry.getValue())
            .collect(Collectors.joining(", "));
        return "= (" + statistics + ") [blocking: " + took + " ms]";
    }

    public abstract void log(String message);
}
