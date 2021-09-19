package eu.okaeri.platform.bungee;

import eu.okaeri.commands.Commands;
import eu.okaeri.configs.serdes.commons.SerdesCommons;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;
import eu.okaeri.injector.Injectable;
import eu.okaeri.injector.Injector;
import eu.okaeri.injector.OkaeriInjector;
import eu.okaeri.persistence.document.ConfigurerProvider;
import eu.okaeri.placeholders.Placeholders;
import eu.okaeri.platform.bungee.component.BungeeComponentCreator;
import eu.okaeri.platform.bungee.component.BungeeCreatorRegistry;
import eu.okaeri.platform.core.component.ComponentHelper;
import eu.okaeri.platform.core.component.ExternalResourceProvider;
import eu.okaeri.platform.core.component.manifest.BeanManifest;
import eu.okaeri.platform.core.exception.BreakException;
import eu.okaeri.platform.core.loader.PlatformPreloader;
import eu.okaeri.platform.minecraft.i18n.I18nCommandsMessages;
import lombok.Getter;
import lombok.SneakyThrows;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginDescription;

import java.util.Collections;
import java.util.Optional;
import java.util.logging.Level;


public class OkaeriBungeePlugin extends Plugin {

    private static final boolean useParallelism = Boolean.parseBoolean(System.getProperty("okaeri.platform.parallelism", "true"));

    private static final ExternalResourceProvider EXTERNAL_RESOURCE_PROVIDER = (name, type, source) -> {

        Plugin plugin = ProxyServer.getInstance().getPluginManager().getPlugins().stream()
                .filter(proxyPlugin -> proxyPlugin.getClass() == source)
                .findAny()
                .orElse(null);

        if (plugin == null) {
            throw new BreakException("Cannot provide external resource: " + name + ", " + type + " from " + source + ": cannot find source");
        }

        Injector externalInjector = ((OkaeriBungeePlugin) plugin).getInjector();
        Optional<? extends Injectable<?>> injectable = externalInjector.getInjectable(name, type);

        if (!injectable.isPresent()) {
            throw new BreakException("Cannot provide external resource: " + name + ", " + type + " from " + source + ": cannot find injectable");
        }

        return injectable.get().getObject();
    };

    @Getter private Injector injector;
    @Getter private Commands commands;

    private BeanManifest beanManifest;
    private BungeeComponentCreator creator;
    private final PlatformPreloader preloader = new PlatformPreloader(this.getLogger(), this.getDescription().getName(), useParallelism, Collections.emptySet());

    @SuppressWarnings("unused")
    public OkaeriBungeePlugin() {
        super();
        this.postBungeeConstruct();
    }

    @SuppressWarnings("unused")
    public OkaeriBungeePlugin(ProxyServer proxy, PluginDescription description) {
        super(proxy, description);
        this.postBungeeConstruct();
    }

    // let's see who is faster
    private void postBungeeConstruct() {

        // setup injector
        this.injector = OkaeriInjector.create(true);
        this.injector.registerInjectable("injector", this.injector);

        // register injectables
        this.injector
                .registerInjectable("proxy", this.getProxy())
                .registerInjectable("dataFolder", this.getDataFolder())
                .registerInjectable("logger", this.getLogger())
                .registerInjectable("plugin", this)
                .registerInjectable("scheduler", this.getProxy().getScheduler())
                .registerInjectable("pluginManager", this.getProxy().getPluginManager())
                .registerInjectable("defaultConfigurerProvider", (ConfigurerProvider) YamlSnakeYamlConfigurer::new)
                .registerInjectable("defaultConfigurerSerdes", new Class[]{SerdesCommons.class});

        // preload
        this.getLogger().info("Preloading " + this.getDescription().getName() + " " + this.getDescription().getVersion());
//        this.preloader.preloadData("Placeholders", () -> this.injector.registerInjectable("placeholders", BukkitPlaceholders.create(true))); TODO: placeholders
        this.preloader.preloadData("BeanManifest", this::preloadManifest);
        this.preloader.preloadData("Config", this::preloadConfig);
        this.preloader.preloadData("LocaleConfig", this::preloadLocaleConfig);
    }

    private void preloadManifest() {
        // commands TODO: commands
//        this.commandsBukkit = CommandsBukkit.of(this).resultHandler(new BukkitCommandsResultHandler());
//        this.commands = CommandsManager.create(CommandsInjector.of(this.commandsBukkit, this.injector)).register(new CommandsBukkitTypes());
//        this.injector.registerInjectable("commands", this.commands);
        // manifest
        this.creator = new BungeeComponentCreator(this, new BungeeCreatorRegistry(this.injector));
        BeanManifest i18CommandsMessages = BeanManifest.of(I18nCommandsMessages.class, this.creator, false).name("i18n-platform-commands");
        this.beanManifest = BeanManifest.of(this.getClass(), this.creator, true).withDepend(i18CommandsMessages);
        this.beanManifest.setObject(this);
    }

    @SneakyThrows
    @SuppressWarnings("BusyWait")
    private void preloadConfig() {
        while (this.beanManifest == null) Thread.sleep(1);
        this.preloader.preloadConfig(this.beanManifest, this.injector, this.creator);
    }

    @SneakyThrows
    @SuppressWarnings("BusyWait")
    private void preloadLocaleConfig() {
        while ((this.beanManifest == null) || (!this.injector.getInjectable("placeholders", Placeholders.class).isPresent())) Thread.sleep(1);
        this.preloader.preloadLocaleConfig(this.beanManifest, this.injector, this.creator);
    }

    @Override
    @SneakyThrows
    public void onEnable() {

        // initialize system
        long start = System.currentTimeMillis();
        this.getLogger().info("Initializing " + this.getClass());

        // fallback load & await all
        this.preloader.fallbackLoadAndAwait();

        // apply i18n text resolver for commands framework TODO: commands
//        Set<BI18n> i18nCommandsProviders = new HashSet<>();
//        AtomicReference<I18nPrefixProvider> prefixProvider = new AtomicReference<>();
//        this.injector.getInjectable("i18n", BI18n.class)
//                .ifPresent(i18n -> {
//                    BI18n bi18n = i18n.getObject();
//                    prefixProvider.set(bi18n.getPrefixProvider());
//                    i18nCommandsProviders.add(bi18n);
//                });
//        this.injector.getInjectable("i18n-platform-commands", BI18n.class)
//                .ifPresent(i18n -> {
//                    BI18n bi18n = i18n.getObject();
//                    I18nPrefixProvider i18nPrefixProvider = prefixProvider.get();
//                    if (i18nPrefixProvider != null) {
//                        bi18n.setPrefixProvider(i18nPrefixProvider);
//                    }
//                    i18nCommandsProviders.add(bi18n);
//                });
//        this.commandsBukkit.textHandler(new I18nCommandsTextHandler(i18nCommandsProviders));

        // dispatch async logs
        // to show that these were loaded
        // before other components here
        this.creator.dispatchLogs();

        // load commands/other beans
        try {
            // execute component tree and register everything
            this.beanManifest.execute(this.creator, this.injector, EXTERNAL_RESOURCE_PROVIDER);
            // sub-components do not require manual injecting because
            // these are filled at the initialization by the DI itself
            // plugin instance however is not, so here it goes
            ComponentHelper.injectComponentFields(this, this.injector);
            // call PostConstruct
            ComponentHelper.invokePostConstruct(this, this.injector);
            // call custom enable method
            this.onPlatformEnable();
        }
        // handle break signal
        catch (BreakException exception) {
            this.getLogger().log(Level.SEVERE, "Stopping initialization, received break signal: " + exception.getMessage());
        }

        // woah
        long took = System.currentTimeMillis() - start;
        this.getLogger().info(this.creator.getSummaryText(took));
    }

    @Override
    public void onDisable() {
        this.onPlatformDisable();
    }

    public void onPlatformEnable() {
    }

    public void onPlatformDisable() {
    }
}
