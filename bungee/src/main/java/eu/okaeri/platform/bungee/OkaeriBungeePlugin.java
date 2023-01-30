package eu.okaeri.platform.bungee;

import eu.okaeri.commands.Commands;
import eu.okaeri.configs.serdes.commons.SerdesCommons;
import eu.okaeri.configs.serdes.okaeri.SerdesOkaeri;
import eu.okaeri.configs.yaml.bukkit.serdes.SerdesBungee;
import eu.okaeri.configs.yaml.bungee.YamlBungeeConfigurer;
import eu.okaeri.injector.Injector;
import eu.okaeri.persistence.Persistence;
import eu.okaeri.persistence.document.ConfigurerProvider;
import eu.okaeri.placeholders.bungee.BungeePlaceholders;
import eu.okaeri.platform.bungee.component.BungeeComponentCreator;
import eu.okaeri.platform.bungee.component.BungeeCreatorRegistry;
import eu.okaeri.platform.bungee.i18n.ProxiedPlayerLocaleProvider;
import eu.okaeri.platform.bungee.plan.BungeeExternalResourceProviderSetupTask;
import eu.okaeri.platform.bungee.plan.BungeeSchedulerShutdownTask;
import eu.okaeri.platform.bungee.scheduler.PlatformScheduler;
import eu.okaeri.platform.core.OkaeriPlatform;
import eu.okaeri.platform.core.component.creator.ComponentCreator;
import eu.okaeri.platform.core.placeholder.SimplePlaceholdersFactory;
import eu.okaeri.platform.core.plan.ExecutionPlan;
import eu.okaeri.platform.core.plan.ExecutionResult;
import eu.okaeri.platform.core.plan.ExecutionTask;
import eu.okaeri.platform.core.plan.task.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginDescription;

import java.util.Arrays;

import static eu.okaeri.platform.core.plan.ExecutionPhase.*;


public class OkaeriBungeePlugin extends Plugin implements OkaeriPlatform {

    private @Getter @Setter Injector injector;
    private @Getter @Setter ComponentCreator creator;
    private ExecutionPlan plan;

    public OkaeriBungeePlugin() {
        super();
    }

    public OkaeriBungeePlugin(ProxyServer proxy, PluginDescription description) {
        super(proxy, description);
    }

    @Override
    public void log(@NonNull String message) {
        this.getLogger().info(message);
    }

    @Override
    public void plan(@NonNull ExecutionPlan plan) {

        plan.add(PRE_SETUP, new InjectorSetupTask());
        plan.add(PRE_SETUP, (ExecutionTask<OkaeriBungeePlugin>) platform -> {
            platform.registerInjectable("proxy", platform.getProxy());
            platform.registerInjectable("dataFolder", platform.getDataFolder());
            platform.registerInjectable("jarFile", platform.getFile());
            platform.registerInjectable("logger", platform.getLogger());
            platform.registerInjectable("plugin", platform);
            platform.registerInjectable("placeholders", BungeePlaceholders.create(true));
            platform.registerInjectable("scheduler", new PlatformScheduler(platform, platform.getProxy().getScheduler()));
            platform.registerInjectable("pluginManager", platform.getProxy().getPluginManager());
            platform.registerInjectable("defaultConfigurerProvider", (ConfigurerProvider) YamlBungeeConfigurer::new);
            platform.registerInjectable("defaultConfigurerSerdes", new Class[]{SerdesCommons.class, SerdesOkaeri.class, SerdesBungee.class});
            platform.registerInjectable("defaultPlaceholdersFactory", new SimplePlaceholdersFactory());
            platform.registerInjectable("i18nLocaleProvider", new ProxiedPlayerLocaleProvider());
        });

        // plan.add(PRE_SETUP, new BungeeCommandsSetupTask()); TODO
        plan.add(SETUP, new CreatorSetupTask(BungeeComponentCreator.class, BungeeCreatorRegistry.class), "creator");

        plan.add(POST_SETUP, new BungeeExternalResourceProviderSetupTask());
        plan.add(POST_SETUP, new BeanManifestCreateTask());
        plan.add(POST_SETUP, new BeanManifestExecuteTask());

        plan.add(SHUTDOWN, new BungeeSchedulerShutdownTask());
        plan.add(SHUTDOWN, new CloseableShutdownTask(Persistence.class));
        plan.add(SHUTDOWN, new CloseableShutdownTask(Commands.class));
    }

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
