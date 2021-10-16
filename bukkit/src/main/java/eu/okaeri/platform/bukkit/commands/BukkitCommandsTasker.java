package eu.okaeri.platform.bukkit.commands;

import eu.okaeri.commands.Commands;
import eu.okaeri.commands.CommandsExtension;
import eu.okaeri.commands.handler.argument.MissingArgumentHandler;
import eu.okaeri.commands.meta.CommandMeta;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.commands.service.InvocationContext;
import eu.okaeri.platform.bukkit.annotation.Chain;
import eu.okaeri.tasker.core.Tasker;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Parameter;

@RequiredArgsConstructor
public class BukkitCommandsTasker implements CommandsExtension {

    private final Tasker tasker;

    @Override
    public void register(Commands commands) {
        MissingArgumentHandler currentHandler = commands.getMissingArgumentHandler();
        commands.missingArgumentHandler(new Handler(currentHandler));
    }

    @RequiredArgsConstructor
    private class Handler implements MissingArgumentHandler {

        private final MissingArgumentHandler parent;

        @Override
        public Object resolve(@NonNull InvocationContext invocationContext, @NonNull CommandContext commandContext, @NonNull CommandMeta command, @NonNull Parameter param, int index) {

            Chain chain = param.getAnnotation(Chain.class);
            if (chain == null) {
                return this.parent.resolve(invocationContext, commandContext, command, param, index);
            }

            String chainName = chain.value();
            if (chainName.isEmpty()) {
                return BukkitCommandsTasker.this.tasker.newChain();
            }

            return BukkitCommandsTasker.this.tasker.newSharedChain(chainName);
        }
    }
}
