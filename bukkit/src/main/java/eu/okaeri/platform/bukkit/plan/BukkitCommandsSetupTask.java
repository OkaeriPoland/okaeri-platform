package eu.okaeri.platform.bukkit.plan;

import eu.okaeri.commands.Commands;
import eu.okaeri.commands.brigadier.CommandsBrigadierPaper;
import eu.okaeri.commands.bukkit.CommandsBukkit;
import eu.okaeri.commands.injector.CommandsInjector;
import eu.okaeri.commands.injector.CommandsValidator;
import eu.okaeri.platform.bukkit.OkaeriBukkitPlugin;
import eu.okaeri.platform.bukkit.commands.BukkitCommandsResultHandler;
import eu.okaeri.platform.bukkit.commands.BukkitCommandsTasker;
import eu.okaeri.platform.core.plan.ExecutionTask;
import eu.okaeri.tasker.core.Tasker;
import eu.okaeri.validator.OkaeriValidator;
import eu.okaeri.validator.policy.NullPolicy;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BukkitCommandsSetupTask implements ExecutionTask<OkaeriBukkitPlugin> {

    private static final boolean BRIGADIER = Boolean.parseBoolean(System.getProperty("okaeri.platform.brigadier", "true"));

    @Override
    public void execute(OkaeriBukkitPlugin platform) {

        // create bukkit commands
        Commands commands = CommandsBukkit.of(platform);

        // register platform specific result handler
        commands.resultHandler(new BukkitCommandsResultHandler());

        // use injector in commands
        commands.registerExtension(new CommandsInjector(platform.getInjector()));

        // enable validation
        commands.registerExtension(new CommandsValidator(OkaeriValidator.of(NullPolicy.NULLABLE)));

        // use brigadier tab-completions if available
        if (BRIGADIER && this.canUseBrigadierPaper()) {
            commands.registerExtension(new CommandsBrigadierPaper(platform));
        }

        // allow @Chain in commands
        platform.getInjector().get("tasker", Tasker.class)
            .ifPresent(tasker -> commands.registerExtension(new BukkitCommandsTasker(tasker)));

        // register commands injectable
        platform.registerInjectable("commands", commands);
    }

    protected boolean canUseBrigadierPaper() {
        try {
            Class.forName("com.destroystokyo.paper.event.brigadier.AsyncPlayerSendCommandsEvent");
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }
}
