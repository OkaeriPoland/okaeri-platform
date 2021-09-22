package eu.okaeri.platform.core.component.manifest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

@Data
@AllArgsConstructor
public class DependencyPair {
    @NonNull private String name;
    @NonNull private Class<?> type;
}
