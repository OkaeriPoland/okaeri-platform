package eu.okaeri.platform.core.component.manifest;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DependencyPair {
    private String name;
    private Class<?> type;
}
