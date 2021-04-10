package eu.okaeri.platform.bukkit;

import eu.okaeri.commands.Commands;
import eu.okaeri.commands.CommandsManager;
import eu.okaeri.commands.bukkit.CommandsBukkit;
import eu.okaeri.commands.injector.CommandsInjector;
import eu.okaeri.injector.Injector;
import eu.okaeri.injector.OkaeriInjector;
import eu.okaeri.platform.core.component.ComponentHelper;
import eu.okaeri.platform.core.component.manifest.BeanManifest;
import eu.okaeri.platform.core.exception.BreakException;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;


public class OkaeriBukkitPlugin extends JavaPlugin {

    @Getter private Injector injector;
    @Getter private Commands commands;

    @Override
    public void onEnable() {

        // initialize system
        long start = System.currentTimeMillis();
        this.getLogger().info("Initializing " + this.getClass());
        this.injector = OkaeriInjector.create(true)
                .registerInjectable("server", this.getServer())
                .registerInjectable("dataFolder", this.getDataFolder())
                .registerInjectable("logger", this.getLogger())
                .registerInjectable("plugin", this)
                .registerInjectable("scheduler", this.getServer().getScheduler())
                .registerInjectable("scoreboardManager", this.getServer().getScoreboardManager())
                .registerInjectable("pluginManager", this.getServer().getPluginManager());

        this.injector.registerInjectable("injector", this.getInjector());
        CommandsBukkit commandsBukkit = CommandsBukkit.of(this).resultHandler(new BukkitCommandsResultHandler());
        this.commands = CommandsManager.create(CommandsInjector.of(commandsBukkit, this.injector));
        this.injector.registerInjectable("commands", this.getCommands());
        BukkitComponentCreator creator = new BukkitComponentCreator(this, this.commands, this.injector);

        // load commands/other beans
        try {
            // scan starting from the current class
            BeanManifest.of(this.getClass(), creator, true).execute(creator, this.injector);
            // sub-components do not require manual injecting because
            // these are filled at the initialization by the DI itself
            // plugin instance however is not, so here it goes
            ComponentHelper.injectComponentFields(this, this.injector);
        }
        // handle break signal
        catch (BreakException exception) {
            this.getLogger().log(Level.SEVERE, "Stopping initialization, received break signal: " + exception.getMessage());
        }

        // woah
        long took = System.currentTimeMillis() - start;
        this.getLogger().info("= (" +
                "configs: " + creator.getLoadedConfigs().size() + ", " +
                "commands: " + creator.getLoadedCommands().size() + ", " +
                "listeners: " + creator.getLoadedListeners().size() + ", " +
                "timers: " + creator.getLoadedTimers().size() + ", " +
                "localeConfigs: " + creator.getLoadedLocaleConfigs().size() +
                ") [" + took + " ms]");

        // call custom enable method
        this.onPlatformEnabled();
    }

    @Override
    public void onDisable() {
        this.onPlatformDisabled();
    }

    public void onPlatformEnabled() {
    }

    public void onPlatformDisabled() {
    }
}
