package eu.okaeri.platform.core.plan.task;

import eu.okaeri.commands.Commands;
import eu.okaeri.commands.CommandsManager;
import eu.okaeri.commands.adapter.CommandsAdapter;
import eu.okaeri.commands.injector.CommandsInjector;
import eu.okaeri.commands.type.CommandsTypesPack;
import eu.okaeri.platform.core.OkaeriPlatform;
import eu.okaeri.platform.core.plan.ExecutionTask;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class CommandsSetupTask implements ExecutionTask<OkaeriPlatform> {

    private final CommandsAdapter adapter;
    private final List<CommandsTypesPack> typePacks;

    public CommandsSetupTask(CommandsAdapter adapter) {
        this.adapter = adapter;
        this.typePacks = Collections.emptyList();
    }

    @Override
    public void execute(OkaeriPlatform platform) {

        CommandsInjector commandsInjector = CommandsInjector.of(this.adapter, platform.getInjector());
        Commands commands = CommandsManager.create(commandsInjector);

        for (CommandsTypesPack typePack : this.typePacks) {
            commands.register(typePack);
        }

        platform.registerInjectable("commands", this.adapter);
        platform.registerInjectable("commands", commands);
    }
}
