package eu.okaeri.platform.core.i18n.message;

import eu.okaeri.i18n.message.Message;
import eu.okaeri.placeholders.Placeholders;
import eu.okaeri.placeholders.message.CompiledMessage;
import lombok.NonNull;

public interface MessageAssembler {
    Message assemble(Placeholders placeholders, @NonNull CompiledMessage compiled);
}
