package eu.okaeri.platform.bukkit.commons.i18n;

import eu.okaeri.commands.handler.TextHandler;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

@RequiredArgsConstructor
public class I18nCommandsTextHandler implements TextHandler {

    private final BI18n i18n;

    @Override
    public String resolve(CommandContext commandContext, InvocationContext invocationContext, String text) {

        if (!text.startsWith("!")) {
            return text;
        }

        String key = text.substring(1);
        CommandSender sender = commandContext.get("sender", CommandSender.class);

        String value = this.i18n.get(sender, key).apply();
        if (("<" + key + ">").equals(value)) {
            return text;
        }

        return ChatColor.stripColor(value);
    }
}
