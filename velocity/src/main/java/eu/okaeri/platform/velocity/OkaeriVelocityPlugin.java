package eu.okaeri.platform.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import eu.okaeri.commands.Commands;
import eu.okaeri.configs.serdes.adventure.SerdesAdventure;
import eu.okaeri.configs.serdes.commons.SerdesCommons;
import eu.okaeri.configs.serdes.okaeri.SerdesOkaeri;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;
import eu.okaeri.injector.Injector;
import eu.okaeri.persistence.Persistence;
import eu.okaeri.persistence.document.ConfigurerProvider;
import eu.okaeri.placeholders.Placeholders;
import eu.okaeri.platform.core.OkaeriPlatform;
import eu.okaeri.platform.core.component.ComponentHelper;
import eu.okaeri.platform.core.component.creator.ComponentCreator;
import eu.okaeri.platform.core.placeholder.SimplePlaceholdersFactory;
import eu.okaeri.platform.core.plan.ExecutionPlan;
import eu.okaeri.platform.core.plan.ExecutionResult;
import eu.okaeri.platform.core.plan.ExecutionTask;
import eu.okaeri.platform.core.plan.task.*;
import eu.okaeri.platform.velocity.component.VelocityComponentCreator;
import eu.okaeri.platform.velocity.component.VelocityCreatorRegistry;
import eu.okaeri.platform.velocity.i18n.PlayerLocaleProvider;
import eu.okaeri.platform.velocity.plan.VelocitySchedulerShutdownTask;
import eu.okaeri.platform.velocity.scheduler.PlatformScheduler;
import eu.okaeri.platform.velocity.util.VelocityUnsafe;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;

import static eu.okaeri.platform.core.plan.ExecutionPhase.*;


public class OkaeriVelocityPlugin implements OkaeriPlatform {

    private final @Getter File file = ComponentHelper.getJarFile(this.getClass());
    private @Getter @Setter Injector injector;
    private @Getter @Setter ComponentCreator creator;
    private ExecutionPlan plan;

    private @Inject @Getter ProxyServer proxy;
    private @Inject @Getter PluginContainer container;
    private @Inject @Getter @DataDirectory Path dataFolder;
    private @Inject @Getter Logger logger;

    @Override
    public void log(@NonNull String message) {
        this.logger.info(message);
    }

    @Override
    public void plan(@NonNull ExecutionPlan plan) {

        plan.add(PRE_SETUP, new InjectorSetupTask());
        plan.add(PRE_SETUP, (ExecutionTask<OkaeriVelocityPlugin>) platform -> {
            platform.registerInjectable("proxy", this.proxy);
            platform.registerInjectable("dataFolder", this.dataFolder);
            platform.registerInjectable("dataFolder", this.dataFolder.toFile());
            platform.registerInjectable("jarFile", this.getFile());
            platform.registerInjectable("logger", this.logger);
            platform.registerInjectable("plugin", this.container);
            platform.registerInjectable("plugin", platform);
            platform.registerInjectable("placeholders", Placeholders.create(true)); // FIXME: velocity placeholders
            platform.registerInjectable("scheduler", new PlatformScheduler(this.container, this.proxy.getScheduler()));
            platform.registerInjectable("pluginManager", this.proxy.getPluginManager());
            platform.registerInjectable("defaultConfigurerProvider", (ConfigurerProvider) YamlSnakeYamlConfigurer::new);
            platform.registerInjectable("defaultConfigurerSerdes", new Class[]{SerdesCommons.class, SerdesOkaeri.class, SerdesAdventure.class});
            platform.registerInjectable("defaultPlaceholdersFactory", new SimplePlaceholdersFactory());
            platform.registerInjectable("i18nLocaleProvider", new PlayerLocaleProvider());
        });

//        plan.add(PRE_SETUP, new VelocityCommandsSetupTask());
        plan.add(SETUP, new CreatorSetupTask(VelocityComponentCreator.class, VelocityCreatorRegistry.class), "creator");

//        plan.add(POST_SETUP, new VelocityExternalResourceProviderSetupTask());
        plan.add(POST_SETUP, new BeanManifestCreateTask());
//        plan.add(POST_SETUP, new VelocityCommandsI18nManifestTask());
        plan.add(POST_SETUP, new BeanManifestExecuteTask());
//        plan.add(POST_SETUP, new CommandsI18nSetupTask());
//        plan.add(POST_SETUP, new CommandsRegisterTask());

        plan.add(SHUTDOWN, new VelocitySchedulerShutdownTask());
        plan.add(SHUTDOWN, new CloseableShutdownTask(Persistence.class));
        plan.add(SHUTDOWN, new CloseableShutdownTask(Commands.class));
    }

    @Subscribe
    public void onEnable(ProxyInitializeEvent event) {
        VelocityUnsafe.PROXY = this.proxy; /// mmmm
        // execute using plan
        this.log("Loading " + this.getClass().getSimpleName());
        ExecutionResult result = ExecutionPlan.dispatch(this);
        this.log(this.getCreator().getSummaryText(result.getTotalMillis()));
        this.plan = result.getPlan();
    }

    @Subscribe
    public void onDisable(ProxyShutdownEvent event) {
        // call shutdown hooks
        if (this.plan != null) {
            this.plan.execute(Arrays.asList(PRE_SHUTDOWN, SHUTDOWN, POST_SHUTDOWN));
        }
    }
}
