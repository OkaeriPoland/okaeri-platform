package eu.okaeri.platform.cli;

import eu.okaeri.commands.Commands;
import eu.okaeri.commands.CommandsManager;
import eu.okaeri.commands.cli.CommandsCli;
import eu.okaeri.commands.injector.CommandsInjector;
import eu.okaeri.configs.serdes.commons.SerdesCommons;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;
import eu.okaeri.injector.Injector;
import eu.okaeri.injector.OkaeriInjector;
import eu.okaeri.persistence.Persistence;
import eu.okaeri.persistence.document.ConfigurerProvider;
import eu.okaeri.placeholders.Placeholders;
import eu.okaeri.platform.cli.component.ApplicationComponentCreator;
import eu.okaeri.platform.cli.component.ApplicationCreatorRegistry;
import eu.okaeri.platform.cli.i18n.SystemLocaleProvider;
import eu.okaeri.platform.core.component.ComponentHelper;
import eu.okaeri.platform.core.component.ExternalResourceProvider;
import eu.okaeri.platform.core.component.manifest.BeanManifest;
import eu.okaeri.platform.core.exception.BreakException;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;


public class OkaeriCliApplication {

    private static final boolean DEBUG = Boolean.parseBoolean(System.getProperty("okaeri.platform.debug", "false"));
    private static final ExternalResourceProvider EXTERNAL_RESOURCE_PROVIDER = (name, type, source) -> null;

    @Getter private final Logger logger = Logger.getLogger(this.getClass().getName());
    @Getter private final File dataFolder = new File(".");
    @Getter private final File jarFile = ComponentHelper.getJarFile(OkaeriCliApplication.class);

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
        this.getInjector().registerInjectable("injector", this.getInjector());

        // register injectables
        this.getInjector()
                .registerInjectable("dataFolder", this.getDataFolder())
                .registerInjectable("jarFile", this.getJarFile())
                .registerInjectable("logger", this.getLogger())
                .registerInjectable("app", this)
                .registerInjectable("placeholders", Placeholders.create(true))
                .registerInjectable("defaultConfigurerProvider", (ConfigurerProvider) YamlSnakeYamlConfigurer::new)
                .registerInjectable("defaultConfigurerSerdes", new Class[]{SerdesCommons.class})
                .registerInjectable("i18nLocaleProvider", new SystemLocaleProvider());

        // commands
        this.commandsCli = new CommandsCli();
        this.commands = CommandsManager.create(CommandsInjector.of(this.getCommandsCli(), this.getInjector()));
        this.getInjector().registerInjectable("commands", this.getCommands());

        // setup creator
        this.creator = new ApplicationComponentCreator(this, new ApplicationCreatorRegistry(this.getInjector()));

        // allow additional setup
        this.setup();

        // loading tasks
        if (DEBUG) this.getLogger().info("Loading " + this.getClass().getSimpleName());
        BeanManifest beanManifest = BeanManifest.of(this.getClass(), this.getCreator(), true);
        beanManifest.setObject(this);

        // load commands/other beans
        try {
            // execute component tree and register everything
            beanManifest.execute(this.getCreator(), this.getInjector(), EXTERNAL_RESOURCE_PROVIDER);
            // sub-components do not require manual injecting because
            // these are filled at the initialization by the DI itself
            // plugin instance however is not, so here it goes
            ComponentHelper.injectComponentFields(this, this.getInjector());
            // call PostConstruct
            ComponentHelper.invokePostConstruct(this, this.getInjector());
            // show platform summary
            long took = System.currentTimeMillis() - start;
            if (DEBUG) this.getLogger().info(this.getCreator().getSummaryText(took));
            // call custom enable method
            this.run(args);
        }
        // handle break signal
        catch (BreakException exception) {
            this.getLogger().log(Level.SEVERE, "Stopping initialization, received break signal: " + exception.getMessage());
        }
    }

    public void callShutdown() {

        // call custom disable method
        this.shutdown();

        // cleanup connections
        ComponentHelper.closeAllOfType(Persistence.class, this.getInjector());
    }

    public void setup() {
    }

    public void run(String... args) {
        try {
            Object result = this.getCommands().call(String.join(" ", args));
            System.out.println(result);
        } catch (Exception exception) {
            this.logger.log(Level.WARNING, "Failed command exectuion", exception);
        }
    }

    public void shutdown() {
    }

    @SneakyThrows
    public static void run(Class<? extends OkaeriCliApplication> type, String[] args) {

        // create instance
        OkaeriCliApplication app = type.newInstance();

        // shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(app::callShutdown));

        // go!
        app.start(args);
    }
}
