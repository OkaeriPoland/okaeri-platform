package eu.okaeri.platform.core.component.manifest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

@Data
@AllArgsConstructor
public class DependencyPair {
    private @NonNull String name;
    private @NonNull Class<?> type;
}
