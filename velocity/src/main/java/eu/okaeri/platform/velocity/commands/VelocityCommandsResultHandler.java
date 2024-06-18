package eu.okaeri.platform.velocity.commands;

import com.velocitypowered.api.command.CommandSource;
import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.Invocation;
import eu.okaeri.commands.velocity.handler.VelocityResultHandler;
import eu.okaeri.i18n.message.Message;
import eu.okaeri.i18n.minecraft.adventure.AdventureMessage;
import eu.okaeri.platform.core.i18n.message.Audience;
import eu.okaeri.tasker.core.chain.TaskerChain;
import lombok.NonNull;
import net.kyori.adventure.text.Component;

import static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection;

public class VelocityCommandsResultHandler extends VelocityResultHandler {

    @Override
    public boolean handle(Object object, @NonNull CommandData data, @NonNull Invocation invocation) {

        if (object == null) {
            return true;
        }

        CommandSource sender = data.get("sender", CommandSource.class);
        if (sender == null) {
            throw new RuntimeException("Cannot return result, no sender found");
        }

        if (object instanceof AdventureMessage) {
            if (!((AdventureMessage) object).raw().isEmpty()) {
                sender.sendMessage(((AdventureMessage) object).component());
            }
            return true;
        }

        if (object instanceof Message) {
            String result = ((Message) object).apply();
            if (!result.isEmpty()) {
                Component component = legacySection().deserialize(result);
                sender.sendMessage(component);
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
