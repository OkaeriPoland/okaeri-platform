package eu.okaeri.platform.bukkit;

import eu.okaeri.configs.serdes.commons.SerdesCommons;
import eu.okaeri.configs.yaml.bukkit.YamlBukkitConfigurer;
import eu.okaeri.configs.yaml.bukkit.serdes.SerdesBukkit;
import eu.okaeri.injector.Injector;
import eu.okaeri.persistence.document.ConfigurerProvider;
import eu.okaeri.placeholders.bukkit.BukkitPlaceholders;
import eu.okaeri.platform.bukkit.component.BukkitComponentCreator;
import eu.okaeri.platform.bukkit.component.BukkitCreatorRegistry;
import eu.okaeri.platform.bukkit.i18n.PlayerLocaleProvider;
import eu.okaeri.platform.bukkit.plan.BukkitCommandsI18nManifestTask;
import eu.okaeri.platform.bukkit.plan.BukkitCommandsSetupTask;
import eu.okaeri.platform.bukkit.plan.BukkitExternalResourceProviderSetupTask;
import eu.okaeri.platform.bukkit.scheduler.PlatformScheduler;
import eu.okaeri.platform.core.OkaeriPlatform;
import eu.okaeri.platform.core.component.ComponentHelper;
import eu.okaeri.platform.core.component.creator.ComponentCreator;
import eu.okaeri.platform.core.placeholder.SimplePlaceholdersFactory;
import eu.okaeri.platform.core.plan.ExecutionPlan;
import eu.okaeri.platform.core.plan.ExecutionResult;
import eu.okaeri.platform.core.plan.ExecutionTask;
import eu.okaeri.platform.core.plan.task.*;
import eu.okaeri.platform.minecraft.task.CommandsI18nSetupTask;
import eu.okaeri.tasker.bukkit.BukkitTasker;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.File;
import java.util.Arrays;

import static eu.okaeri.platform.core.plan.ExecutionPhase.*;


public class OkaeriBukkitPlugin extends JavaPlugin implements OkaeriPlatform {

    @Getter private final File file = ComponentHelper.getJarFile(OkaeriBukkitPlugin.class);
    @Getter @Setter private Injector injector;
    @Getter @Setter private ComponentCreator creator;

    public OkaeriBukkitPlugin() {
        super();
    }

    protected OkaeriBukkitPlugin(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
        super(loader, description, dataFolder, file);
    }

    @Override
    public void log(@NonNull String message) {
        this.getLogger().info(message);
    }

    @Override
    public void plan(@NonNull ExecutionPlan plan) {

        plan.add(PRE_SETUP, new InjectorSetupTask());
        plan.add(PRE_SETUP, (ExecutionTask<OkaeriBukkitPlugin>) platform -> {
            platform.registerInjectable("server", platform.getServer());
            platform.registerInjectable("dataFolder", platform.getDataFolder());
            platform.registerInjectable("jarFile", platform.getFile());
            platform.registerInjectable("logger", platform.getLogger());
            platform.registerInjectable("plugin", platform);
            platform.registerInjectable("placeholders", BukkitPlaceholders.create(true));
            platform.registerInjectable("scheduler", new PlatformScheduler(platform, platform.getServer().getScheduler()));
            platform.registerInjectable("tasker", BukkitTasker.newPool(platform));
            platform.registerInjectable("pluginManager", platform.getServer().getPluginManager());
            platform.registerInjectable("defaultConfigurerProvider", (ConfigurerProvider) YamlBukkitConfigurer::new);
            platform.registerInjectable("defaultConfigurerSerdes", new Class[]{SerdesCommons.class, SerdesBukkit.class});
            platform.registerInjectable("defaultPlaceholdersFactory", new SimplePlaceholdersFactory());
            platform.registerInjectable("i18nLocaleProvider", new PlayerLocaleProvider());
        });

        plan.add(PRE_SETUP, new BukkitCommandsSetupTask());
        plan.add(PRE_SETUP, new CreatorSetupTask(BukkitComponentCreator.class, BukkitCreatorRegistry.class));

        plan.add(POST_SETUP, new BukkitExternalResourceProviderSetupTask());
        plan.add(POST_SETUP, new BeanManifestCreateTask());
        plan.add(POST_SETUP, new BukkitCommandsI18nManifestTask());
        plan.add(POST_SETUP, new BeanManifestExecuteTask());
        plan.add(POST_SETUP, new CommandsI18nSetupTask());

        plan.add(SHUTDOWN, new PersistenceShutdownTask());
    }

    private ExecutionPlan plan;

    @Override
    @Deprecated
    public void onEnable() {
        // execute using plan
        this.log("Loading " + this.getClass().getSimpleName());
        ExecutionResult result = ExecutionPlan.dispatch(this);
        this.log(this.getCreator().getSummaryText(result.getTotalMillis()));
        this.plan = result.getPlan();
        // compatibility
        this.onPlatformEnable();
    }

    @Override
    @Deprecated
    public void onDisable() {
        // call shutdown hooks
        if (this.plan != null) {
            this.plan.execute(Arrays.asList(PRE_SHUTDOWN, SHUTDOWN, POST_SHUTDOWN));
        }
        // compatibility
        this.onPlatformDisable();
    }

    /**
     * @deprecated Use method annotated with `@Planned(ExecutionPhase.STARTUP)`.
     */
    @Deprecated
    public void onPlatformEnable() {
    }

    /**
     * @deprecated Use method annotated with `@Planned(ExecutionPhase.SHUTDOWN)`.
     */
    @Deprecated
    public void onPlatformDisable() {
    }
}
