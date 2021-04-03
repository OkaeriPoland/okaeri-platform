package eu.okaeri.platform.bukkit;

import eu.okaeri.commands.CommandsManager;
import eu.okaeri.commands.OkaeriCommands;
import eu.okaeri.commands.bukkit.CommandsBukkit;
import eu.okaeri.commands.injector.CommandsInjector;
import eu.okaeri.injector.Injector;
import eu.okaeri.injector.OkaeriInjector;
import eu.okaeri.platform.core.component.manifest.BeanManifest;
import eu.okaeri.platform.core.exception.BreakException;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;


public class OkaeriBukkitPlugin extends JavaPlugin {

    @Getter private Injector injector;
    @Getter private OkaeriCommands commands;

    @Override
    public void onEnable() {
        this.postBukkitInitialize();
        this.onPlatformEnabled();
    }

    @Override
    public void onDisable() {
        this.onPlatformDisabled();
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    private void postBukkitInitialize() {

        // initialize system
        this.getLogger().info("Initializing " + this.getClass());
        this.injector = OkaeriInjector.create().registerInjectable(this);
        this.commands = CommandsManager.create(CommandsInjector.of(CommandsBukkit.of(this), this.injector));
        BukkitComponentCreator creator = new BukkitComponentCreator(this, this.commands);

        // load commands/other beans
        try {
            BeanManifest.of(this.getClass(), creator).execute(creator, this.injector);
        } catch (BreakException exception) {
            this.getLogger().log(Level.SEVERE, "Stopping initialization, received break signal: " + exception.getMessage());
        }
    }

    public void onPlatformEnabled() {
    }

    public void onPlatformDisabled() {
    }
}
