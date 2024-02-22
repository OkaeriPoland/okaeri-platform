package eu.okaeri.platform.bungee.commands;

import eu.okaeri.commands.Commands;
import eu.okaeri.commands.CommandsExtension;
import eu.okaeri.commands.handler.argument.MissingArgumentHandler;
import eu.okaeri.commands.meta.CommandMeta;
import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.Invocation;
import eu.okaeri.platform.bungee.annotation.Chain;
import eu.okaeri.tasker.core.Tasker;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Parameter;

@RequiredArgsConstructor
public class BungeeCommandsTasker implements CommandsExtension {

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
        public Object resolve(@NonNull Invocation invocation, @NonNull CommandData data, @NonNull CommandMeta command, @NonNull Parameter param, int index) {

            Chain chain = param.getAnnotation(Chain.class);
            if (chain == null) {
                return this.parent.resolve(invocation, data, command, param, index);
            }

            String chainName = chain.value();
            if (chainName.isEmpty()) {
                return BungeeCommandsTasker.this.tasker.newChain();
            }

            return BungeeCommandsTasker.this.tasker.newSharedChain(chainName);
        }
    }
}
