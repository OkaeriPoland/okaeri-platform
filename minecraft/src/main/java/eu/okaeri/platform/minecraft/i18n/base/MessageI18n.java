package eu.okaeri.platform.minecraft.i18n.base;

import eu.okaeri.i18n.message.Message;
import eu.okaeri.placeholders.Placeholders;
import eu.okaeri.placeholders.message.CompiledMessage;
import eu.okaeri.placeholders.message.part.MessageElement;
import eu.okaeri.placeholders.message.part.MessageStatic;
import eu.okaeri.platform.minecraft.i18n.I18nPrefixProvider;
import eu.okaeri.validator.annotation.Nullable;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
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
}
