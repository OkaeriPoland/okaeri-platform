package eu.okaeri.platform.bukkit.i18n.minedown;

import de.themoep.minedown.MineDown;
import eu.okaeri.i18n.message.Message;
import eu.okaeri.placeholders.Placeholders;
import eu.okaeri.placeholders.context.PlaceholderContext;
import eu.okaeri.placeholders.message.CompiledMessage;
import eu.okaeri.validator.annotation.Nullable;
import lombok.NonNull;
import net.md_5.bungee.api.chat.BaseComponent;

import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class ComponentMessage extends Message {

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

        MineDown minedown = new MineDown(this.raw())
            .placeholderPrefix("{")
            .placeholderSuffix("}");

        minedown.replace(this.context.renderFields().entrySet().stream()
            .collect(Collectors.toMap(
                entry -> entry.getKey().getSource(),
                Map.Entry::getValue
            )));

        return minedown.toComponent();
    }

    @Override
    public String apply() {
        return BaseComponent.toLegacyText(this.components());
    }
}
