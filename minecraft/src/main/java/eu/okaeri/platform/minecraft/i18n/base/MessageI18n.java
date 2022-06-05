package eu.okaeri.platform.minecraft.i18n.base;

import eu.okaeri.i18n.message.Message;
import eu.okaeri.placeholders.Placeholders;
import eu.okaeri.placeholders.message.CompiledMessage;
import eu.okaeri.platform.minecraft.i18n.I18nPrefixProvider;
import eu.okaeri.validator.annotation.Nullable;
import lombok.NonNull;

import java.util.Locale;

public abstract class MessageI18n extends MinecraftI18n<Message> {

    public MessageI18n(String prefixField, String prefixMarker, Placeholders placeholders, I18nPrefixProvider prefixProvider) {
        super(prefixField, prefixMarker, placeholders, prefixProvider);
    }

    public MessageI18n(String prefixField, String prefixMarker) {
        super(prefixField, prefixMarker);
    }

    @Override
    public CompiledMessage storeConfigValue(@NonNull Locale locale, @NonNull Object value) {
        return CompiledMessage.of(locale, String.valueOf(value));
    }

    @Override
    public Message createMessageFromStored(@Nullable CompiledMessage object, @NonNull String key) {
        if (object == null) {
            return Message.of("<" + key + ">");
        }
        return Message.of(this.placeholders, object);
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
}
