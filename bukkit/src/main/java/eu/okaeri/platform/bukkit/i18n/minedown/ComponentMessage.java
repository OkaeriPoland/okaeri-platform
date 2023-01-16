package eu.okaeri.platform.bukkit.i18n.minedown;

import eu.okaeri.i18n.message.Message;
import eu.okaeri.placeholders.Placeholders;
import eu.okaeri.placeholders.context.PlaceholderContext;
import eu.okaeri.placeholders.message.CompiledMessage;
import eu.okaeri.validator.annotation.Nullable;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ComponentMessage extends Message {

    private static final Pattern SECTION_COLOR_PATTERN = Pattern.compile("(?i)§([0-9A-FK-OR])");

    private static final Pattern ALL_TEXT_PATTERN = Pattern.compile(".*");
    private static final Pattern FIELD_PATTERN = Pattern.compile("\\{(?<content>[^}]+)\\}");

    private static final LegacyComponentSerializer SECTION_SERIALIZER = LegacyComponentSerializer.legacySection();
    private static final LegacyComponentSerializer AMPERSAND_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();
    private static final GsonComponentSerializer GSON_SERIALIZER = GsonComponentSerializer.gson();

    private static final TextReplacementConfig COLOR_REPLACEMENTS = TextReplacementConfig.builder()
        .match(ALL_TEXT_PATTERN)
        .replacement((result, input) -> AMPERSAND_SERIALIZER.deserialize(result.group()))
        .build();
    private static final MiniMessage MINI_MESSAGE = MiniMessage.builder()
        .preProcessor(text -> SECTION_COLOR_PATTERN.matcher(text).replaceAll("&$1")) // convert section to ampersand
        .postProcessor(component -> component.replaceText(COLOR_REPLACEMENTS))
        .build();

    ComponentMessage(@NonNull CompiledMessage compiled, @NonNull PlaceholderContext context) {
        super(compiled, context);
    }

    @Deprecated
    public static ComponentMessage of(@NonNull String raw) {
        return of(null, CompiledMessage.of(raw));
    }

    public static ComponentMessage of(@NonNull Locale locale, @NonNull String raw) {
        return of(null, locale, raw);
    }

    @Deprecated
    public static ComponentMessage of(Placeholders placeholders, @NonNull String raw) {
        return of(placeholders, CompiledMessage.of(raw));
    }

    public static ComponentMessage of(Placeholders placeholders, @NonNull Locale locale, @NonNull String raw) {
        return of(placeholders, CompiledMessage.of(locale, raw));
    }

    public static ComponentMessage of(Placeholders placeholders, @NonNull CompiledMessage compiled) {

        PlaceholderContext context = (placeholders == null)
            ? PlaceholderContext.of(compiled)
            : placeholders.contextOf(compiled);

        return new ComponentMessage(compiled, context);
    }

    @Override
    public ComponentMessage with(@NonNull String field, @Nullable Object value) {
        return (ComponentMessage) super.with(field, value);
    }

    @Override
    public ComponentMessage with(@NonNull Map<String, Object> fields) {
        return (ComponentMessage) super.with(fields);
    }

    @Override
    public ComponentMessage with(@NonNull PlaceholderContext context) {
        return (ComponentMessage) super.with(context);
    }

    public BaseComponent[] components() {

        Map<String, String> renderedFields = this.context.renderFields()
            .entrySet()
            .stream()
            .collect(Collectors.toMap(
                entry -> entry.getKey().getSource(),
                Map.Entry::getValue
            ));

        TextReplacementConfig replacer = TextReplacementConfig.builder()
            .match(FIELD_PATTERN)
            .replacement((result, input) -> {
                String fieldValue = renderedFields.get(result.group(1));
                return SECTION_SERIALIZER.deserialize(fieldValue);
            })
            .build();

        Component component = MINI_MESSAGE.deserialize(this.raw()).replaceText(replacer);
        return ComponentSerializer.parse(GSON_SERIALIZER.serialize(component));
    }

    @Override
    public String apply() {
        return BaseComponent.toLegacyText(this.components());
    }
}
