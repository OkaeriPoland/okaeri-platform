package eu.okaeri.platform.bukkit.commands;

import eu.okaeri.commands.bukkit.handler.BukkitResultHandler;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;
import eu.okaeri.i18n.message.Message;
import eu.okaeri.platform.core.i18n.message.Audience;
import eu.okaeri.tasker.core.chain.TaskerChain;
import lombok.NonNull;
import org.bukkit.command.CommandSender;

public class BukkitCommandsResultHandler extends BukkitResultHandler {

    @Override
    public boolean handle(Object object, @NonNull CommandContext commandContext, @NonNull InvocationContext invocationContext) {

        CommandSender sender = commandContext.get("sender", CommandSender.class);
        if (sender == null) {
            throw new RuntimeException("Cannot return result, no sender found");
        }

        if (object instanceof Message) {
            String result = ((Message) object).apply();
            if (!result.isEmpty()) {
                sender.sendMessage(result);
            }
            return true;
        }

        if (object instanceof Audience) {
            ((Audience) object).close();
            return true;
        }

        if (object instanceof TaskerChain) {
            ((TaskerChain<?>) object).execute();
            return true;
        }

        return super.handle(object, commandContext, invocationContext);
    }
}
