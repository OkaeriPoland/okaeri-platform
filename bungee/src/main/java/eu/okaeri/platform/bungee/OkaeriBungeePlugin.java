package eu.okaeri.platform.bungee;

import eu.okaeri.commands.Commands;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.i18n.configs.LocaleConfig;
import eu.okaeri.injector.Injectable;
import eu.okaeri.injector.Injector;
import eu.okaeri.injector.OkaeriInjector;
import eu.okaeri.platform.bungee.i18n.I18nCommandsMessages;
import eu.okaeri.platform.core.component.ComponentHelper;
import eu.okaeri.platform.core.component.ExternalResourceProvider;
import eu.okaeri.platform.core.component.manifest.BeanManifest;
import eu.okaeri.platform.core.exception.BreakException;
import lombok.*;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginDescription;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Collectors;


public class OkaeriBungeePlugin extends Plugin {

    private static boolean useParallelism = Boolean.parseBoolean(System.getProperty("okaeri.platform.parallelism", "true"));

    @SuppressWarnings("unchecked") private static final ExternalResourceProvider EXTERNAL_RESOURCE_PROVIDER = (name, type, source) -> {

        Plugin plugin = ProxyServer.getInstance().getPluginManager().getPlugins().stream()
                .filter(proxyPlugin -> proxyPlugin.getClass() == source)
                .findAny()
                .orElse(null);

        if (plugin == null) {
            throw new BreakException("cannot provide external resource: " + name + ", " + type + " from " + source + ": cannot find source");
        }

        Injector externalInjector = ((OkaeriBungeePlugin) plugin).getInjector();
        Optional<? extends Injectable<?>> injectable = externalInjector.getInjectable(name, type);

        if (!injectable.isPresent()) {
            throw new BreakException("cannot provide external resource: " + name + ", " + type + " from " + source + ": cannot find injectable");
        }

        return injectable.get().getObject();
    };

    @Getter private Injector injector;
    @Getter private Commands commands;
//    @Getter private CommandsBukkit commandsBukkit; TODO: commands

    private BeanManifest beanManifest;
    private BukkitComponentCreator creator;
    private List<AsyncLoader> preloaders = Collections.synchronizedList(new ArrayList<>());

    public OkaeriBungeePlugin() {
        this.postBungeeConstruct();
    }

    public OkaeriBungeePlugin(ProxyServer proxy, PluginDescription description) {
        super(proxy, description);
        this.postBungeeConstruct();
    }

    @Data
    @RequiredArgsConstructor
    class AsyncLoader {
        private final String name;
        private final Runnable runnable;
        private Thread thread;
        private boolean done;
    }

    // let's see who is faster
    private void postBungeeConstruct() {
        this.getLogger().info("Preloading " + this.getDescription().getName() + " " + this.getDescription().getVersion());
//        this.preloadData("Placeholders", () -> BukkitComponentCreator.defaultPlaceholders = BukkitPlaceholders.create(true)); TODO: placeholders
        this.preloadData("BeanManifest", this::preloadManifest);
        this.preloadData("Config", this::preloadConfig);
        this.preloadData("LocaleConfig", this::preloadLocaleConfig);
    }

    private Thread createPreloadThread(@NonNull String name, @NonNull Runnable runnable) {
        Thread preloader = new Thread(runnable);
        preloader.setName("Okaeri Platform Preloader (" + this.getDescription().getName() + ") - " + name);
        return preloader;
    }

    private void preloadData(@NonNull String name, @NonNull Runnable runnable) {

        AsyncLoader asyncLoader = new AsyncLoader(name, runnable);
        Thread preloader = this.createPreloadThread(name, () -> {
            try {
                runnable.run();
                asyncLoader.setDone(true);
            } catch (Throwable exception) {
                this.getLogger().warning(name + ": " + exception.getMessage());
            }
        });

        asyncLoader.setThread(preloader);
        this.preloaders.add(asyncLoader);
        if (useParallelism) preloader.start();
    }

    private void preloadManifest() {
        // injector
        this.injector = OkaeriInjector.create(true);
        this.injector.registerInjectable("injector", this.injector);
        // commands TODO: commands
//        this.commandsBukkit = CommandsBukkit.of(this).resultHandler(new BukkitCommandsResultHandler());
//        this.commands = CommandsManager.create(CommandsInjector.of(this.commandsBukkit, this.injector)).register(new CommandsBukkitTypes());
//        this.injector.registerInjectable("commands", this.commands);
        // manifest
        this.creator = new BukkitComponentCreator(this, this.commands, this.injector);
        BeanManifest i18CommandsMessages = BeanManifest.of(I18nCommandsMessages.class, this.creator, false).name("i18n-platform-commands");
        this.beanManifest = BeanManifest.of(this.getClass(), this.creator, true).withDepend(i18CommandsMessages);
    }

    @SneakyThrows
    private void preloadConfig() {

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
    private void preloadLocaleConfig() {

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

        // fallback load
        this.preloaders.stream()
                .filter(loader -> !loader.getThread().isAlive())
                .filter(loader -> !loader.isDone())
                .map(loader -> {
                    this.getLogger().warning("- Fallback loading (async fail): " + loader.getName());
                    return loader.getRunnable();
                })
                .forEach(Runnable::run);

        // wait if not initialized yet
        for (Thread preloader : this.preloaders.stream().map(AsyncLoader::getThread).collect(Collectors.toList())) {
            if (useParallelism) preloader.join();
            else preloader.run();
        }

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
        if (this.creator != null) this.creator.dispatchLogs();

        // register injectables
        this.injector
                .registerInjectable("proxy", this.getProxy())
                .registerInjectable("dataFolder", this.getDataFolder())
                .registerInjectable("logger", this.getLogger())
                .registerInjectable("plugin", this)
                .registerInjectable("scheduler", this.getProxy().getScheduler())
                .registerInjectable("pluginManager", this.getProxy().getPluginManager());

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
            this.onPlatformEnabled();
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
