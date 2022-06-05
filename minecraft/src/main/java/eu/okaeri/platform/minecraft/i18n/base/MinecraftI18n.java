package eu.okaeri.platform.minecraft.i18n.base;

import eu.okaeri.configs.schema.FieldDeclaration;
import eu.okaeri.i18n.configs.LocaleConfig;
import eu.okaeri.i18n.configs.OCI18n;
import eu.okaeri.i18n.message.MessageDispatcher;
import eu.okaeri.placeholders.Placeholders;
import eu.okaeri.placeholders.message.CompiledMessage;
import eu.okaeri.platform.minecraft.i18n.I18nMessageColors;
import eu.okaeri.platform.minecraft.i18n.I18nPrefixProvider;
import lombok.*;

import java.nio.file.Files;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@AllArgsConstructor
@RequiredArgsConstructor
public abstract class MinecraftI18n<T> extends OCI18n<CompiledMessage, T, MessageDispatcher<T>> {

    private static final Pattern MESSAGE_FIELD_PATTERN = Pattern.compile("\\{[^{]+}");

    protected final @Getter Map<Locale, LocaleConfig> configs = new HashMap<>();
    protected final @Getter Map<Locale, Map<String, Object>> rawConfigs = new HashMap<>();

    protected final @Getter String prefixField;
    protected final @Getter String prefixMarker;

    protected @Getter @Setter Placeholders placeholders;
    protected @Getter @Setter I18nPrefixProvider prefixProvider;

    @Override
    public MinecraftI18n<T> registerConfig(@NonNull Locale locale, @NonNull LocaleConfig config) {
        this.rawConfigs.putIfAbsent(locale, config.asMap(config.getConfigurer(), true));
        this.update(locale, config);
        this.configs.put(locale, config);
        return (MinecraftI18n<T>) super.registerConfig(locale, config);
    }

    protected void update(@NonNull Locale locale, @NonNull LocaleConfig config) {

        // load from file
        if ((config.getBindFile() != null) && Files.exists(config.getBindFile())) {
            config.load(true);
        }
        // use previous known if available
        else if (this.rawConfigs.containsKey(locale)) {
            config.load(this.rawConfigs.get(locale));
        }

        // update fields
        for (FieldDeclaration field : config.getDeclaration().getFields()) {

            if (!(field.getValue() instanceof String)) {
                continue;
            }

            String fieldName = field.getName().toLowerCase(Locale.ROOT);
            String fieldValue = String.valueOf(field.getValue());

            // ignore prefix/coloring of empty messages
            if (fieldValue.isEmpty()) {
                continue;
            }

            // resolve prefix or empty if not applicable
            String localPrefix = "";
            if (fieldValue.startsWith(this.getPrefixMarker())) {
                fieldValue = fieldValue.substring(this.getPrefixMarker().length());
                localPrefix = this.getPrefixMarker();
            }

            // do not auto-color messages with predefined colors
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

        String prefix = this.color((String) config.getDeclaration().getFields().stream()
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
