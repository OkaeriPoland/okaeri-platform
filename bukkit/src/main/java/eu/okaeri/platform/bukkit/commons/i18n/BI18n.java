package eu.okaeri.platform.bukkit.commons.i18n;

import eu.okaeri.configs.schema.ConfigDeclaration;
import eu.okaeri.configs.schema.FieldDeclaration;
import eu.okaeri.i18n.configs.LocaleConfig;
import eu.okaeri.i18n.configs.OCI18n;
import eu.okaeri.i18n.configs.impl.MOCI18n;
import eu.okaeri.i18n.message.Message;
import eu.okaeri.placeholders.message.CompiledMessage;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class BI18n extends MOCI18n {

    private static final Pattern ALT_COLOR_PATTERN = Pattern.compile("&[0-9A-Fa-fK-Ok-oRXrx]");
    private static final Pattern MESSAGE_FIELD_PATTERN = Pattern.compile("\\{[^{]+\\}");
    private final Map<Locale, LocaleConfig> configs = new HashMap<>();
    private final I18nColorsConfig colorsConfig;

    @Override
    public OCI18n<CompiledMessage, Message> registerConfig(Locale locale, LocaleConfig config) {
        this.update(config);
        this.configs.put(locale, config);
        return super.registerConfig(locale, config);
    }

    public void load() {
        for (Map.Entry<Locale, LocaleConfig> entry : this.configs.entrySet()) {
            LocaleConfig config = entry.getValue();
            this.update(config);
            super.registerConfig(entry.getKey(), config);
        }
    }

    private void update(LocaleConfig config) {

        if (config.getBindFile() != null) config.load(true);
        ConfigDeclaration declaration = config.getDeclaration();

        for (FieldDeclaration field : declaration.getFields()) {

            if (!(field.getValue() instanceof String)) {
                continue;
            }

            String fieldName = field.getName().toLowerCase(Locale.ROOT);
            String fieldValue = String.valueOf(field.getValue());

            // normalnie pokolorowane - ignorujemy
            if (this.hasColors(fieldValue)) {
                field.updateValue(ChatColor.translateAlternateColorCodes('&', fieldValue));
                continue;
            }

            // kolorujemy
            for (I18nColorMatcher matcher : this.colorsConfig.getMatchers()) {
                if (!matcher.getPattern().matcher(fieldName).matches()) {
                    continue;
                }
                field.updateValue(matcher.getMessageColor() + fieldValue);
                break;
            }
        }
    }

    private boolean hasColors(String text) {
        return ALT_COLOR_PATTERN.matcher(text).find();
    }
}
