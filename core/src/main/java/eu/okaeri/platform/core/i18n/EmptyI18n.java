package eu.okaeri.platform.core.i18n;

import eu.okaeri.i18n.configs.extended.CustomMEOCI18n;
import eu.okaeri.i18n.message.Message;
import eu.okaeri.placeholders.Placeholders;
import eu.okaeri.placeholders.message.CompiledMessage;
import lombok.NonNull;

public class EmptyI18n<T extends Message> extends CustomMEOCI18n<T> {

    @Override
    public T assembleMessage(Placeholders placeholders, @NonNull CompiledMessage compiled) {
        throw new RuntimeException("EmptyI18n is not intended to be used");
    }
}
