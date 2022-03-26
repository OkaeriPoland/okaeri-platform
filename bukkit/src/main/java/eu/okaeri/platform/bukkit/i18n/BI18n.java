package eu.okaeri.platform.bukkit.i18n;

import eu.okaeri.i18n.configs.LocaleConfig;
import eu.okaeri.i18n.message.Message;
import eu.okaeri.platform.bukkit.i18n.message.BukkitMessageDispatcher;
import eu.okaeri.platform.core.placeholder.PlaceholdersFactory;
import eu.okaeri.platform.minecraft.i18n.I18nMessageColors;
import eu.okaeri.platform.minecraft.i18n.MI18n;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.ChatColor;

import java.nio.file.Files;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public class BI18n extends MI18n {

    private static final Pattern ALT_COLOR_PATTERN = Pattern.compile("&[0-9A-Fa-fK-Ok-oRXrx]");

    private final @Getter I18nColorsConfig colorsConfig;
    private final @Getter PlaceholdersFactory placeholdersFactory;

    public BI18n(@NonNull I18nColorsConfig colorsConfig, @NonNull String prefixField, @NonNull String prefixMarker, @NonNull PlaceholdersFactory placeholdersFactory) {
        super(prefixField, prefixMarker);
        this.colorsConfig = colorsConfig;
        this.placeholdersFactory = placeholdersFactory;
    }

    @Override
    public BukkitMessageDispatcher get(@NonNull String key) {
        return new BukkitMessageDispatcher(this, key, this.getPlaceholders(), this.getPlaceholdersFactory());
    }

    @Override
    public Message get(@NonNull Object entity, @NonNull String key) {
        Message message = super.get(entity, key);
        this.getPlaceholdersFactory().provide(entity).forEach(message::with);
        return message;
    }

    @Override
    public void load() {

        if ((this.getColorsConfig().getBindFile() != null) && Files.exists(this.getColorsConfig().getBindFile())) {
            this.getColorsConfig().load();
        }

        for (Map.Entry<Locale, LocaleConfig> entry : this.getConfigs().entrySet()) {
            super.registerConfig(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public boolean hasColors(@NonNull String text) {
        return ALT_COLOR_PATTERN.matcher(text).find();
    }

    @Override
    public String color(String source) {
        return ChatColor.translateAlternateColorCodes('&', source);
    }

    @Override
    protected Optional<I18nMessageColors> matchColors(String fieldName) {
        return this.getColorsConfig().getMatchers().stream()
            .filter(matcher -> matcher.getPattern().matcher(fieldName).matches())
            .map(matcher -> I18nMessageColors.of(String.valueOf(matcher.getMessageColor()), String.valueOf(matcher.getFieldsColor())))
            .findAny();
    }
}
