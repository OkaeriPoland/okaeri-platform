package eu.okaeri.platform.core.plan.task;

import eu.okaeri.commands.Commands;
import eu.okaeri.commands.service.CommandService;
import eu.okaeri.platform.core.OkaeriPlatform;
import eu.okaeri.platform.core.plan.ExecutionTask;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommandsRegisterTask implements ExecutionTask<OkaeriPlatform> {

    @Override
    public void execute(OkaeriPlatform platform) {
        Commands commands = platform.getInjector().getOrThrow("commands", Commands.class);
        platform.getInjector().streamOf(CommandService.class).forEach(commands::registerCommand);
    }
}
