package eu.okaeri.platform.minecraft.i18n;

@FunctionalInterface
public interface I18nPrefixProvider {
    String getPrefix(Object entity, String key);
}
