package eu.okaeri.platform.web;

import com.fasterxml.jackson.databind.json.JsonMapper;
import eu.okaeri.commands.Commands;
import eu.okaeri.commands.CommandsManager;
import eu.okaeri.commands.cli.CommandsCli;
import eu.okaeri.commands.injector.CommandsInjector;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.serdes.commons.SerdesCommons;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;
import eu.okaeri.injector.Injector;
import eu.okaeri.injector.OkaeriInjector;
import eu.okaeri.persistence.Persistence;
import eu.okaeri.persistence.document.ConfigurerProvider;
import eu.okaeri.persistence.document.Document;
import eu.okaeri.placeholders.Placeholders;
import eu.okaeri.platform.core.component.ComponentHelper;
import eu.okaeri.platform.core.component.ExternalResourceProvider;
import eu.okaeri.platform.core.component.manifest.BeanManifest;
import eu.okaeri.platform.core.exception.BreakException;
import eu.okaeri.platform.web.component.ApplicationComponentCreator;
import eu.okaeri.platform.web.component.ApplicationCreatorRegistry;
import eu.okaeri.platform.web.i18n.SystemLocaleProvider;
import eu.okaeri.platform.web.persistence.DocumentMixIn;
import eu.okaeri.platform.web.persistence.OkaeriConfigMixIn;
import io.javalin.Javalin;
import io.javalin.plugin.json.JavalinJackson;
import lombok.Getter;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;


public class OkaeriWebApplication {

    private static final ExternalResourceProvider EXTERNAL_RESOURCE_PROVIDER = (name, type, source) -> null;

    @Getter private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Getter private final File dataFolder = new File(".");
    @Getter private final File jarFile = ComponentHelper.getJarFile(OkaeriWebApplication.class);
    @Getter private final Javalin javalin = this.setupJavalin();

    @Getter private Injector injector;
    @Getter private Commands commands;
    @Getter private CommandsCli commandsCli;
    @Getter private ApplicationComponentCreator creator;

    @SneakyThrows
    public void start(String... args) {

        // start timing
        long start = System.currentTimeMillis();

        // setup injector
        this.injector = OkaeriInjector.create(true);
        this.injector.registerInjectable("injector", this.injector);

        // register injectables
        this.injector
                .registerInjectable("dataFolder", this.getDataFolder())
                .registerInjectable("jarFile", this.getJarFile())
                .registerInjectable("logger", this.getLogger())
                .registerInjectable("app", this)
                .registerInjectable("javalin", this.getJavalin())
                .registerInjectable("placeholders", Placeholders.create(true))
                .registerInjectable("defaultConfigurerProvider", (ConfigurerProvider) YamlSnakeYamlConfigurer::new)
                .registerInjectable("defaultConfigurerSerdes", new Class[]{SerdesCommons.class})
                .registerInjectable("i18nLocaleProvider", new SystemLocaleProvider());

        // commands
        this.commandsCli = new CommandsCli();
        this.commands = CommandsManager.create(CommandsInjector.of(this.commandsCli, this.injector));
        this.injector.registerInjectable("commands", this.commands);

        // setup creator
        this.creator = new ApplicationComponentCreator(this, new ApplicationCreatorRegistry(this.injector));

        // allow additional setup
        this.setup();

        // loading tasks
        this.getLogger().info("Loading " + this.getClass().getSimpleName());
        BeanManifest beanManifest = BeanManifest.of(this.getClass(), this.creator, true);
        beanManifest.setObject(this);

        // load commands/other beans
        try {
            // execute component tree and register everything
            beanManifest.execute(this.creator, this.injector, EXTERNAL_RESOURCE_PROVIDER);
            // sub-components do not require manual injecting because
            // these are filled at the initialization by the DI itself
            // plugin instance however is not, so here it goes
            ComponentHelper.injectComponentFields(this, this.injector);
            // call PostConstruct
            ComponentHelper.invokePostConstruct(this, this.injector);
            // show platform summary
            long took = System.currentTimeMillis() - start;
            this.getLogger().info(this.creator.getSummaryText(took));
            // call custom enable method
            this.run(args);
        }
        // handle break signal
        catch (BreakException exception) {
            this.getLogger().error("Stopping initialization, received break signal: " + exception.getMessage());
        }
    }

    public void callShutdown() {

        // shutdown javalin
        this.javalin.stop();

        // call custom disable method
        this.shutdown();

        // cleanup connections
        ComponentHelper.closeAllOfType(Persistence.class, this.injector);
    }

    public void setup() {
    }

    public Javalin setupJavalin() {
        return Javalin.create(config -> {
            JsonMapper jsonMapper = new JsonMapper();
            jsonMapper.addMixIn(Document.class, DocumentMixIn.class);
            jsonMapper.addMixIn(OkaeriConfig.class, OkaeriConfigMixIn.class);
            config.jsonMapper(new JavalinJackson(jsonMapper));
        });
    }

    public void run(String... args) {
        this.javalin.start();
    }

    public void shutdown() {
    }

    @SneakyThrows
    public static void run(Class<? extends OkaeriWebApplication> type, String[] args) {

        // create instance
        OkaeriWebApplication app = type.newInstance();

        // shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(app::callShutdown));

        // go!
        app.start(args);
    }
}
