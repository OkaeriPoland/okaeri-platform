package eu.okaeri.platform.web;

import eu.okaeri.commands.OkaeriCommands;
import eu.okaeri.configs.serdes.commons.SerdesCommons;
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
import eu.okaeri.platform.standalone.i18n.SystemLocaleProvider;
import eu.okaeri.platform.web.component.WebCreatorRegistry;
import eu.okaeri.platform.web.meta.serdes.SerdesWeb;
import eu.okaeri.platform.web.plan.JavalinSetupTask;
import eu.okaeri.platform.web.plan.JavalinShutdownTask;
import eu.okaeri.platform.web.plan.JavalinStartTask;
import io.javalin.Javalin;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

import static eu.okaeri.platform.core.plan.ExecutionPhase.*;


public class OkaeriWebApplication implements OkaeriPlatform {

    @Getter private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Getter private final File dataFolder = new File(".");
    @Getter private final File file = ComponentHelper.getJarFile(OkaeriWebApplication.class);

    @Getter private final Javalin javalin = Javalin.create();

    @Getter @Setter private Injector injector;
    @Getter @Setter private ComponentCreator creator;

    @Override
    public void log(@NonNull String message) {
        this.getLogger().info(message);
    }

    @Override
    public void plan(@NonNull ExecutionPlan plan) {

        plan.add(PRE_SETUP, new InjectorSetupTask());
        plan.add(PRE_SETUP, (ExecutionTask<OkaeriWebApplication>) platform -> {
            platform.registerInjectable("dataFolder", platform.getDataFolder());
            platform.registerInjectable("jarFile", platform.getFile());
            platform.registerInjectable("logger", platform.getLogger());
            platform.registerInjectable("app", platform);
            platform.registerInjectable("javalin", platform.getJavalin());
            platform.registerInjectable("jetty", Objects.requireNonNull(platform.getJavalin().jettyServer()));
            platform.registerInjectable("placeholders", Placeholders.create(true));
            platform.registerInjectable("defaultConfigurerProvider", (ConfigurerProvider) YamlSnakeYamlConfigurer::new);
            platform.registerInjectable("defaultConfigurerSerdes", new Class[]{SerdesCommons.class, SerdesWeb.class});
            platform.registerInjectable("defaultPlaceholdersFactory", new SimplePlaceholdersFactory());
            platform.registerInjectable("i18nLocaleProvider", new SystemLocaleProvider());
        });

        plan.add(SETUP, new CommandsSetupTask(new OkaeriCommands()));
        plan.add(SETUP, new CreatorSetupTask(ApplicationComponentCreator.class, WebCreatorRegistry.class));

        plan.add(POST_SETUP, new BeanManifestCreateTask());
        plan.add(POST_SETUP, new BeanManifestExecuteTask());
        plan.add(POST_SETUP, new JavalinSetupTask());
        plan.add(POST_SETUP, new PlatformBannerStartupTask());

        plan.add(STARTUP, new JavalinStartTask());

        plan.add(SHUTDOWN, new JavalinShutdownTask());
        plan.add(SHUTDOWN, new PersistenceShutdownTask());
    }

    public static <T extends OkaeriWebApplication> T run(@NonNull T app, @NonNull String[] args) {

        ExecutionResult result = ExecutionPlan.dispatch(app);
        app.log(app.getCreator().getSummaryText(result.getTotalMillis()));

        Thread shutdownHook = new Thread(() -> result.getPlan().execute(Arrays.asList(PRE_SHUTDOWN, SHUTDOWN, POST_SHUTDOWN)));
        Runtime.getRuntime().addShutdownHook(shutdownHook);

        return app;
    }

    @SneakyThrows
    public static <T extends OkaeriWebApplication> T run(@NonNull Class<? extends T> type, @NonNull String[] args) {
        return run(type.newInstance(), args);
    }
}
