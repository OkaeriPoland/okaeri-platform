package eu.okaeri.platform.core.component;

import lombok.NonNull;

@FunctionalInterface
public interface ExternalResourceProvider {
    Object provide(@NonNull String name, @NonNull Class<?> type, @NonNull Class<?> of);
}
