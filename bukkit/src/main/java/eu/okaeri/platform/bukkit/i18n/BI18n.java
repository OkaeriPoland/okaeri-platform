package eu.okaeri.platform.bukkit.i18n;

import eu.okaeri.i18n.configs.LocaleConfig;
import eu.okaeri.platform.minecraft.i18n.I18nMessageColors;
import eu.okaeri.platform.minecraft.i18n.MI18n;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.ChatColor;

import java.nio.file.Files;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public class BI18n extends MI18n {

    private static final Pattern ALT_COLOR_PATTERN = Pattern.compile("&[0-9A-Fa-fK-Ok-oRXrx]");

    @Getter private final Map<Locale, LocaleConfig> configs = new HashMap<>();
    @Getter private final I18nColorsConfig colorsConfig;

    public BI18n(I18nColorsConfig colorsConfig, String prefixField, String prefixMarker) {
        super(prefixField, prefixMarker);
        this.colorsConfig = colorsConfig;
    }

    @Override
    public void load() {

        if ((this.colorsConfig.getBindFile() != null) && Files.exists(this.colorsConfig.getBindFile())) {
            this.colorsConfig.load();
        }

        for (Map.Entry<Locale, LocaleConfig> entry : this.configs.entrySet()) {
            LocaleConfig config = entry.getValue();
            this.update(config);
            super.registerConfig(entry.getKey(), config);
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
        return this.colorsConfig.getMatchers().stream()
                .filter(matcher -> matcher.getPattern().matcher(fieldName).matches())
                .map(matcher -> I18nMessageColors.of(String.valueOf(matcher.getMessageColor()), String.valueOf(matcher.getFieldsColor())))
                .findAny();
    }
}
