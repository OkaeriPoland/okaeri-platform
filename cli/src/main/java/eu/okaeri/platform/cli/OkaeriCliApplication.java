package eu.okaeri.platform.cli;

import eu.okaeri.commands.OkaeriCommands;
import eu.okaeri.configs.serdes.commons.SerdesCommons;
import eu.okaeri.configs.serdes.okaeri.SerdesOkaeri;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;
import eu.okaeri.injector.Injector;
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
import eu.okaeri.platform.standalone.component.ApplicationComponentCreator;
import eu.okaeri.platform.standalone.component.ApplicationCreatorRegistry;
import eu.okaeri.platform.standalone.i18n.SystemLocaleProvider;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;

import java.io.File;
import java.util.Arrays;
import java.util.logging.Logger;

import static eu.okaeri.platform.core.plan.ExecutionPhase.*;


public class OkaeriCliApplication implements OkaeriPlatform {

    private static final boolean DEBUG = Boolean.parseBoolean(System.getProperty("okaeri.platform.debug", "false"));
    private static String[] args;

    private final @Getter Logger logger = Logger.getLogger(OkaeriCliApplication.class.getName());
    private final @Getter File dataFolder = new File(".");
    private final @Getter File file = ComponentHelper.getJarFile(OkaeriCliApplication.class);

    private @Getter @Setter Injector injector;
    private @Getter @Setter ComponentCreator creator;
    private @Getter @Setter boolean verbose = true;

    @Override
    public void log(@NonNull String message) {
        if (!DEBUG && !this.isVerbose()) {
            return;
        }
        this.getLogger().info(message);
    }

    @Override
    public void plan(@NonNull ExecutionPlan plan) {

        plan.add(PRE_SETUP, new InjectorSetupTask());
        plan.add(PRE_SETUP, (ExecutionTask<OkaeriCliApplication>) platform -> {
            platform.registerInjectable("dataFolder", platform.getDataFolder());
            platform.registerInjectable("jarFile", platform.getFile());
            platform.registerInjectable("logger", platform.getLogger());
            platform.registerInjectable("app", platform);
            platform.registerInjectable("placeholders", Placeholders.create(true));
            platform.registerInjectable("defaultConfigurerProvider", (ConfigurerProvider) YamlSnakeYamlConfigurer::new);
            platform.registerInjectable("defaultConfigurerSerdes", new Class[]{SerdesCommons.class, SerdesOkaeri.class});
            platform.registerInjectable("defaultPlaceholdersFactory", new SimplePlaceholdersFactory());
            platform.registerInjectable("i18nLocaleProvider", new SystemLocaleProvider());
        });

        plan.add(PRE_SETUP, platform -> platform.registerInjectable("args", args));
        plan.add(SETUP, new CommandsSetupTask(new OkaeriCommands()));
        plan.add(SETUP, new CreatorSetupTask(ApplicationComponentCreator.class, ApplicationCreatorRegistry.class));

        plan.add(POST_SETUP, new BeanManifestCreateTask());
        plan.add(POST_SETUP, new BeanManifestExecuteTask());
        plan.add(POST_SETUP, new PlatformBannerStartupTask());

        plan.add(SHUTDOWN, new PersistenceShutdownTask());
    }

    public static <T extends OkaeriCliApplication> T run(@NonNull T app, @NonNull String[] args) {

        OkaeriCliApplication.args = args;
        ExecutionResult result = ExecutionPlan.dispatch(app);
        app.log(app.getCreator().getSummaryText(result.getTotalMillis()));

        Thread shutdownHook = new Thread(() -> result.getPlan().execute(Arrays.asList(PRE_SHUTDOWN, SHUTDOWN, POST_SHUTDOWN)));
        Runtime.getRuntime().addShutdownHook(shutdownHook);

        return app;
    }

    @SneakyThrows
    public static <T extends OkaeriCliApplication> T run(@NonNull Class<? extends T> type, @NonNull String[] args) {
        return run(type.getConstructor().newInstance(), args);
    }
}
