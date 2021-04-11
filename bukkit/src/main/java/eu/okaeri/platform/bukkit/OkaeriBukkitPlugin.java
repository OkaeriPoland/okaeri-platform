package eu.okaeri.platform.bukkit;

import eu.okaeri.commands.Commands;
import eu.okaeri.commands.CommandsManager;
import eu.okaeri.commands.bukkit.CommandsBukkit;
import eu.okaeri.commands.injector.CommandsInjector;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.i18n.configs.LocaleConfig;
import eu.okaeri.injector.Injector;
import eu.okaeri.injector.OkaeriInjector;
import eu.okaeri.placeholders.bukkit.BukkitPlaceholders;
import eu.okaeri.platform.core.component.ComponentHelper;
import eu.okaeri.platform.core.component.manifest.BeanManifest;
import eu.okaeri.platform.core.exception.BreakException;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;


public class OkaeriBukkitPlugin extends JavaPlugin {

    @Getter private Injector injector;
    @Getter private Commands commands;

    private BeanManifest beanManifest;
    private BukkitComponentCreator creator;
    private List<Thread> preloaders = new ArrayList<>();

    public OkaeriBukkitPlugin() {
        this.postBukkitConstruct();
    }

    public OkaeriBukkitPlugin(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
        super(loader, description, dataFolder, file);
        this.postBukkitConstruct();
    }

    // let's see who is faster
    private void postBukkitConstruct() {
        this.getLogger().info("Preloading " + this.getName() + " " + this.getDescription().getVersion());
        this.preloadData("Placeholders", () -> BukkitComponentCreator.defaultPlaceholders = BukkitPlaceholders.create(true));
        this.preloadData("BeanManifest", () -> {
            this.injector = OkaeriInjector.create(true);
            this.injector.registerInjectable("injector", this.injector);
            CommandsBukkit commandsBukkit = CommandsBukkit.of(this).resultHandler(new BukkitCommandsResultHandler());
            this.commands = CommandsManager.create(CommandsInjector.of(commandsBukkit, this.injector));
            this.injector.registerInjectable("commands", this.commands);
            this.creator = new BukkitComponentCreator(this, this.commands, this.injector);
            this.beanManifest = BeanManifest.of(this.getClass(), this.creator, true);
        });
        this.preloadData("Config", () -> this.preloadConfig(this.beanManifest));
        this.preloadData("LocaleConfig", () -> this.preloadLocaleConfig(this.beanManifest));
    }

    private Thread createPreloadThread(String name, Runnable runnable) {
        Thread preloader = new Thread(runnable);
        preloader.setName("Okaeri Platform Preloader (" + this.getName() + ") - " + name);
        return preloader;
    }

    private void preloadData(String name, Runnable runnable) {
        Thread preloader = this.createPreloadThread(name, runnable);
        this.preloaders.add(preloader);
        preloader.start();
    }

    @SneakyThrows
    private void preloadConfig(BeanManifest beanManifest) {

        while (this.beanManifest == null) Thread.sleep(1);
        List<BeanManifest> depends = this.beanManifest.getDepends();

        for (BeanManifest depend : depends) {

            if (!OkaeriConfig.class.isAssignableFrom(depend.getType()) // is not okaeri config
                    || LocaleConfig.class.isAssignableFrom(depend.getType()) // or is locale config
                    || !depend.ready(this.injector)) { // or is not ready (somehow has dependencies)
                continue;
            }

            depend.setObject(this.creator.makeObject(depend, this.injector));
            this.injector.registerInjectable(depend.getName(), depend.getObject());
        }
    }

    @SneakyThrows
    private void preloadLocaleConfig(BeanManifest beanManifest) {

        while ((this.beanManifest == null) || (BukkitComponentCreator.defaultPlaceholders == null)) Thread.sleep(1);
        List<BeanManifest> depends = this.beanManifest.getDepends();

        for (BeanManifest depend : depends) {
            if (!LocaleConfig.class.isAssignableFrom(depend.getType())  // is not locale config
                    || !depend.ready(this.injector)) { // or is not ready (somehow has dependencies)
                continue;
            }
            depend.setObject(this.creator.makeObject(depend, this.injector));
            this.injector.registerInjectable(depend.getName(), depend.getObject());
        }
    }

    @Override
    @SneakyThrows
    public void onEnable() {

        // initialize system
        long start = System.currentTimeMillis();
        this.getLogger().info("Initializing " + this.getClass());

        // dispatch async logs
        // to show that these were loaded
        // before other components here
        this.creator.dispatchLogs();

        // wait if not initialized yet
        for (Thread preloader : this.preloaders) preloader.join();

        // register injectables
        this.injector
                .registerInjectable("server", this.getServer())
                .registerInjectable("dataFolder", this.getDataFolder())
                .registerInjectable("logger", this.getLogger())
                .registerInjectable("plugin", this)
                .registerInjectable("scheduler", this.getServer().getScheduler())
                .registerInjectable("scoreboardManager", this.getServer().getScoreboardManager())
                .registerInjectable("pluginManager", this.getServer().getPluginManager());

        // load commands/other beans
        try {
            // scan starting from the current class
            this.beanManifest.execute(this.creator, this.injector);
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
                "configs: " + this.creator.getLoadedConfigs().size() + ", " +
                "commands: " + this.creator.getLoadedCommands().size() + ", " +
                "listeners: " + this.creator.getLoadedListeners().size() + ", " +
                "timers: " + this.creator.getLoadedTimers().size() + ", " +
                "localeConfigs: " + this.creator.getLoadedLocaleConfigs().size() +
                ") [blocking: " + took + " ms]");

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
