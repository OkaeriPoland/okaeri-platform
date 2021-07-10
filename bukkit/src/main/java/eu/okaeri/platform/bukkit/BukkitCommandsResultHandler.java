package eu.okaeri.platform.bukkit;

import eu.okaeri.commands.bukkit.handler.DefaultResultHandler;
import eu.okaeri.commands.handler.ResultHandler;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;
import eu.okaeri.i18n.message.Message;
import lombok.NonNull;
import org.bukkit.command.CommandSender;

public class BukkitCommandsResultHandler implements ResultHandler {

    private static final DefaultResultHandler DEFAULT_RESULT_HANDLER = new DefaultResultHandler();

    @Override
    public boolean onResult(Object object, @NonNull CommandContext commandContext, @NonNull InvocationContext invocationContext) {

        CommandSender sender = commandContext.get("sender", CommandSender.class);
        if (sender == null) {
            throw new RuntimeException("cannot return result, no sender found");
        }

        if (object instanceof Message) {
            sender.sendMessage(((Message) object).apply());
            return true;
        }

        return DEFAULT_RESULT_HANDLER.onResult(object, commandContext, invocationContext);
    }
}
