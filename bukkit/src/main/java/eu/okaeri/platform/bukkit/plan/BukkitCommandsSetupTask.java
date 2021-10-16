package eu.okaeri.platform.bukkit.plan;

import eu.okaeri.commands.Commands;
import eu.okaeri.commands.brigadier.CommandsBrigadierPaper;
import eu.okaeri.commands.bukkit.CommandsBukkit;
import eu.okaeri.commands.injector.CommandsInjector;
import eu.okaeri.platform.bukkit.OkaeriBukkitPlugin;
import eu.okaeri.platform.bukkit.commands.BukkitCommandsResultHandler;
import eu.okaeri.platform.core.plan.ExecutionTask;
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

        // use brigadier tab-completions if available
        if (BRIGADIER && this.canUseBrigadierPaper()) {
            commands.registerExtension(new CommandsBrigadierPaper(platform));
        }

        // register commands injectable
        platform.registerInjectable("commands", commands);
    }

    protected boolean canUseBrigadierPaper() {
        try {
            Class.forName("com.destroystokyo.paper.event.brigadier.AsyncPlayerSendCommandsEvent");
            return true;
        }catch (ClassNotFoundException ignored) {
            return false;
        }
    }
}
