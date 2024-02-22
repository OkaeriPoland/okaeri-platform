package eu.okaeri.platform.bukkit.commands;

import eu.okaeri.commands.bukkit.handler.BukkitResultHandler;
import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.Invocation;
import eu.okaeri.commons.bukkit.UnsafeBukkitCommons;
import eu.okaeri.i18n.message.Message;
import eu.okaeri.i18n.minecraft.bungee.BungeeMessage;
import eu.okaeri.platform.core.i18n.message.Audience;
import eu.okaeri.tasker.core.chain.TaskerChain;
import lombok.NonNull;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BukkitCommandsResultHandler extends BukkitResultHandler {

    @Override
    public boolean handle(Object object, @NonNull CommandData data, @NonNull Invocation invocation) {

        CommandSender sender = data.get("sender", CommandSender.class);
        if (sender == null) {
            throw new RuntimeException("Cannot return result, no sender found");
        }

        if (object instanceof BungeeMessage) {
            if (sender instanceof Player) {
                if (!((BungeeMessage) object).raw().isEmpty()) {
                    if (((BungeeMessage) object).isSimple()) {
                        sender.sendMessage(((BungeeMessage) object).apply());
                    } else {
                        UnsafeBukkitCommons.sendComponent((Player) sender, ((BungeeMessage) object).component(), UnsafeBukkitCommons.ChatTarget.CHAT);
                    }
                }
            } else {
                String result = ((BungeeMessage) object).apply();
                if (!result.isEmpty()) {
                    sender.sendMessage(result);
                }
            }
            return true;
        }

        if (object instanceof Message) {
            String result = ((Message) object).apply();
            if (!result.isEmpty()) {
                sender.sendMessage(result);
            }
            return true;
        }

        if (object instanceof Audience) {
            ((Audience<?>) object).close();
            return true;
        }

        if (object instanceof TaskerChain) {
            ((TaskerChain<?>) object).execute();
            return true;
        }

        return super.handle(object, data, invocation);
    }
}
