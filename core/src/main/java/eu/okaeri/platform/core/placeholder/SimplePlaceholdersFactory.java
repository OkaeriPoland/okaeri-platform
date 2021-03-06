package eu.okaeri.platform.core.placeholder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimplePlaceholdersFactory implements PlaceholdersFactory {

    private Map<String, Object> defaultPlaceholders = new LinkedHashMap<>();

    @Override
    public Map<String, Object> provide(@NonNull Object entity) {
        return this.defaultPlaceholders;
    }
}
