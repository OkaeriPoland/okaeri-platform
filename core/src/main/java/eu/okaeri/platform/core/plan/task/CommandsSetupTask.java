package eu.okaeri.platform.core.plan.task;

import eu.okaeri.commands.Commands;
import eu.okaeri.commands.injector.CommandsInjector;
import eu.okaeri.platform.core.OkaeriPlatform;
import eu.okaeri.platform.core.plan.ExecutionTask;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommandsSetupTask implements ExecutionTask<OkaeriPlatform> {

    private final Commands commands;

    @Override
    public void execute(OkaeriPlatform platform) {
        this.commands.registerExtension(new CommandsInjector(platform.getInjector()));
        platform.registerInjectable("commands", this.commands);
    }
}
