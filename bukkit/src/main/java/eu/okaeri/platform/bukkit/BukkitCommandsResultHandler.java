package eu.okaeri.platform.bukkit;

import eu.okaeri.commands.bukkit.handler.DefaultResultHandler;
import eu.okaeri.commands.bukkit.handler.ResultHandler;
import eu.okaeri.i18n.message.Message;
import org.bukkit.command.CommandSender;

public class BukkitCommandsResultHandler implements ResultHandler {

    private static final DefaultResultHandler DEFAULT_RESULT_HANDLER = new DefaultResultHandler();

    @Override
    public boolean onResult(Object object, CommandSender sender) {

        if (object instanceof Message) {
            sender.sendMessage(((Message) object).apply());
            return true;
        }

        return DEFAULT_RESULT_HANDLER.onResult(object, sender);
    }
}
