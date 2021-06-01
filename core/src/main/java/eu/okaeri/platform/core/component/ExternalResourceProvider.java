package eu.okaeri.platform.core.component;

public interface ExternalResourceProvider {
    Object provide(String name, Class<?> type, Class<?> of);
}
