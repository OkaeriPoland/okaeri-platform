package eu.okaeri.platform.bukkit.commons.i18n;

import eu.okaeri.commands.handler.TextHandler;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;
import eu.okaeri.i18n.message.Message;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
public class I18nCommandsTextHandler implements TextHandler {

    private final Set<BI18n> i18n;

    public I18nCommandsTextHandler(BI18n i18n) {
        this(new HashSet<>(Collections.singletonList(i18n)));
    }

    @Override
    public String resolve(CommandContext commandContext, InvocationContext invocationContext, String text) {

        if (!text.startsWith("!")) {
            return text;
        }

        String key = text.substring(1);
        CommandSender sender = commandContext.get("sender", CommandSender.class);
        String value = null;

        for (BI18n i18n : this.i18n) {
            Message message = i18n.get(sender, key);
            value = message.raw();
            if (this.isValid(value, key)) break;
        }

        return this.isValid(value, key) ? value : text;
    }

    private boolean isValid(String value, String key) {
        return (value != null) && !("<" + key + ">").equals(value);
    }
}
