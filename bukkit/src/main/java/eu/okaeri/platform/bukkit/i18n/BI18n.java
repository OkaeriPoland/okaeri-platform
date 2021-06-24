package eu.okaeri.platform.bukkit.i18n;

import eu.okaeri.configs.schema.ConfigDeclaration;
import eu.okaeri.configs.schema.FieldDeclaration;
import eu.okaeri.i18n.configs.LocaleConfig;
import eu.okaeri.i18n.configs.OCI18n;
import eu.okaeri.i18n.configs.impl.MOCI18n;
import eu.okaeri.i18n.message.Message;
import eu.okaeri.placeholders.message.CompiledMessage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class BI18n extends MOCI18n {

    private static final Pattern ALT_COLOR_PATTERN = Pattern.compile("&[0-9A-Fa-fK-Ok-oRXrx]");
    private static final Pattern MESSAGE_FIELD_PATTERN = Pattern.compile("\\{[^{]+\\}");

    @Getter private final Map<Locale, LocaleConfig> configs = new HashMap<>();
    @Getter private final I18nColorsConfig colorsConfig;
    private final String prefixField;
    private final String prefixMarker;

    @Getter @Setter
    private I18nPrefixProvider prefixProvider;

    @Override
    public OCI18n<CompiledMessage, Message> registerConfig(Locale locale, LocaleConfig config) {
        this.update(config);
        this.configs.put(locale, config);
        return super.registerConfig(locale, config);
    }

    public void load() {

        if ((this.colorsConfig.getBindFile() != null) && this.colorsConfig.getBindFile().exists()) {
            this.colorsConfig.load();
        }

        for (Map.Entry<Locale, LocaleConfig> entry : this.configs.entrySet()) {
            LocaleConfig config = entry.getValue();
            this.update(config);
            super.registerConfig(entry.getKey(), config);
        }
    }

    @Override
    public Message get(Object entity, String key) {

        Message message = super.get(entity, key);
        String raw = message.raw();

        if (raw.startsWith(this.prefixMarker) && (this.prefixProvider != null)) {
            raw = raw.substring(this.prefixMarker.length());
            String prefix = this.prefixProvider.getPrefix(entity, key);
            return Message.of(this.getPlaceholders(), prefix + raw);
        }

        return message;
    }

    private void update(LocaleConfig config) {

        if ((config.getBindFile() != null) && config.getBindFile().exists()) config.load(true);
        ConfigDeclaration declaration = config.getDeclaration();

        for (FieldDeclaration field : declaration.getFields()) {

            if (!(field.getValue() instanceof String)) {
                continue;
            }

            String fieldName = field.getName().toLowerCase(Locale.ROOT);
            String fieldValue = String.valueOf(field.getValue());

            // prefix
            String localPrefix = "";
            if (fieldValue.startsWith(this.prefixMarker)) {
                fieldValue = fieldValue.substring(this.prefixMarker.length());
                localPrefix = this.prefixMarker;
            }

            // already contains colors - ignore
            if (this.hasColors(fieldValue)) {
                field.updateValue(localPrefix + ChatColor.translateAlternateColorCodes('&', fieldValue));
                continue;
            }

            // add colors based on the matchers
            for (I18nColorMatcher matcher : this.colorsConfig.getMatchers()) {

                // matcher does not match, continue
                if (!matcher.getPattern().matcher(fieldName).matches()) {
                    continue;
                }

                // fields color
                if (matcher.getFieldsColor() != null) {
                    fieldValue = MESSAGE_FIELD_PATTERN.matcher(fieldValue)
                            .replaceAll(matcher.getFieldsColor() + "$0" + matcher.getMessageColor());
                }

                // message color
                field.updateValue(localPrefix + matcher.getMessageColor() + fieldValue);
                break;
            }
        }

        String prefix = ChatColor.translateAlternateColorCodes('&', (String) declaration.getFields().stream()
                .filter(field -> this.prefixField.equals(field.getField().getName()))
                .findFirst()
                .map(FieldDeclaration::getValue)
                .orElse(""));
        this.prefixProvider = (entity, key) -> prefix;
    }

    private boolean hasColors(String text) {
        return ALT_COLOR_PATTERN.matcher(text).find();
    }
}
