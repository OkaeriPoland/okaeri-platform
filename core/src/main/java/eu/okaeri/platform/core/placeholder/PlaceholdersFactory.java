package eu.okaeri.platform.core.placeholder;

import lombok.NonNull;

import java.util.Map;

public interface PlaceholdersFactory {
    Map<String, Object> provide(@NonNull Object entity);
}
