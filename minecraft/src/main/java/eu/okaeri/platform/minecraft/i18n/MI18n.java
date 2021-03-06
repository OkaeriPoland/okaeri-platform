package eu.okaeri.platform.minecraft.i18n;

import eu.okaeri.configs.schema.FieldDeclaration;
import eu.okaeri.i18n.configs.LocaleConfig;
import eu.okaeri.i18n.configs.impl.MOCI18n;
import eu.okaeri.i18n.message.Message;
import eu.okaeri.placeholders.message.CompiledMessage;
import eu.okaeri.placeholders.message.part.MessageElement;
import eu.okaeri.placeholders.message.part.MessageStatic;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.nio.file.Files;
import java.util.*;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public abstract class MI18n extends MOCI18n {

    private static final Pattern MESSAGE_FIELD_PATTERN = Pattern.compile("\\{[^{]+}");

    private final @Getter Map<Locale, LocaleConfig> configs = new HashMap<>();
    private final @Getter Map<Locale, Map<String, Object>> rawConfigs = new HashMap<>();

    private final @Getter String prefixField;
    private final @Getter String prefixMarker;
    private @Getter @Setter I18nPrefixProvider prefixProvider;

    @Override
    public MI18n registerConfig(@NonNull Locale locale, @NonNull LocaleConfig config) {
        this.rawConfigs.putIfAbsent(locale, config.asMap(config.getConfigurer(), true));
        this.update(locale, config);
        this.configs.put(locale, config);
        return (MI18n) super.registerConfig(locale, config);
    }

    @Override
    public Message get(@NonNull Object entity, @NonNull String key) {

        Message message = super.get(entity, key);
        if (this.getPrefixProvider() == null) {
            return message;
        }

        List<MessageElement> messageElements = message.compiled().getParts();
        if (messageElements.isEmpty()) {
            return message;
        }

        MessageElement firstElement = messageElements.get(0);
        if (!(firstElement instanceof MessageStatic)) {
            return message;
        }

        MessageStatic staticElement = (MessageStatic) firstElement;
        String elementValue = staticElement.getValue();

        if (!elementValue.startsWith(this.getPrefixMarker())) {
            return message;
        }

        String prefix = this.getPrefixProvider().getPrefix(entity, key);
        String base = elementValue.substring(this.getPrefixMarker().length());

        List<MessageElement> elementsCopy = new ArrayList<>(messageElements);
        elementsCopy.set(0, MessageStatic.of(prefix + base));

        String raw = prefix + message.raw().substring(this.getPrefixMarker().length()); // wat
        CompiledMessage compiled = CompiledMessage.of(raw, elementsCopy);

        return Message.of(this.getPlaceholders(), compiled);
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
