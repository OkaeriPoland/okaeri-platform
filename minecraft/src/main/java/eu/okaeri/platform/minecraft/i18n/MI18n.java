package eu.okaeri.platform.minecraft.i18n;

import eu.okaeri.configs.schema.ConfigDeclaration;
import eu.okaeri.configs.schema.FieldDeclaration;
import eu.okaeri.i18n.configs.LocaleConfig;
import eu.okaeri.i18n.configs.OCI18n;
import eu.okaeri.i18n.configs.impl.MOCI18n;
import eu.okaeri.i18n.message.Message;
import eu.okaeri.placeholders.message.CompiledMessage;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.nio.file.Files;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public abstract class MI18n extends MOCI18n {

    private static final Pattern MESSAGE_FIELD_PATTERN = Pattern.compile("\\{[^{]+}");

    @Getter private final Map<Locale, LocaleConfig> configs = new HashMap<>();
    @Getter private final String prefixField;
    @Getter private final String prefixMarker;

    @Getter @Setter
    private I18nPrefixProvider prefixProvider;

    @Override
    public OCI18n<CompiledMessage, Message> registerConfig(@NonNull Locale locale, @NonNull LocaleConfig config) {
        this.update(config);
        this.configs.put(locale, config);
        return super.registerConfig(locale, config);
    }

    @Override
    public Message get(@NonNull Object entity, @NonNull String key) {

        Message message = super.get(entity, key);
        String raw = message.raw();

        if (raw.startsWith(this.getPrefixMarker()) && (this.getPrefixProvider() != null)) {
            raw = raw.substring(this.getPrefixMarker().length());
            String prefix = this.getPrefixProvider().getPrefix(entity, key);
            return Message.of(this.getPlaceholders(), prefix + raw);
        }

        return message;
    }

    protected void update(@NonNull LocaleConfig config) {

        if ((config.getBindFile() != null) && Files.exists(config.getBindFile())) config.load(true);
        ConfigDeclaration declaration = config.getDeclaration();

        for (FieldDeclaration field : declaration.getFields()) {

            if (!(field.getValue() instanceof String)) {
                continue;
            }

            String fieldName = field.getName().toLowerCase(Locale.ROOT);
            String fieldValue = String.valueOf(field.getValue());

            // prefix
            String localPrefix = "";
            if (fieldValue.startsWith(this.getPrefixMarker())) {
                fieldValue = fieldValue.substring(this.getPrefixMarker().length());
                localPrefix = this.getPrefixMarker();
            }

            // already contains colors - ignore
            if (this.hasColors(fieldValue)) {
                field.updateValue(localPrefix + this.color(fieldValue));
                continue;
            }

            // add colors based on the matchers
            Optional<I18nMessageColors> colorsOptional = this.matchColors(fieldName);
            if (colorsOptional.isPresent()) {
                // fields + message color
                I18nMessageColors colors = colorsOptional.get();
                if (colors.getFieldsColor() != null) {
                    fieldValue = MESSAGE_FIELD_PATTERN.matcher(fieldValue).replaceAll(colors.getFieldsColor() + "$0" + colors.getMessageColor());
                }
                // just message color
                field.updateValue(localPrefix + colors.getMessageColor() + fieldValue);
            }
        }

        String prefix = this.color((String) declaration.getFields().stream()
                .filter(field -> this.getPrefixField().equals(field.getField().getName()))
                .findFirst()
                .map(FieldDeclaration::getValue)
                .orElse(""));
        this.setPrefixProvider((entity, key) -> prefix);
    }

    public abstract void load();

    protected abstract boolean hasColors(String text);

    public abstract String color(String source);

    protected abstract Optional<I18nMessageColors> matchColors(String fieldName);
}
