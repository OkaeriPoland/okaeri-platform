package eu.okaeri.platform.bukkit;

import eu.okaeri.commands.Commands;
import eu.okaeri.commands.CommandsManager;
import eu.okaeri.commands.bukkit.CommandsBukkit;
import eu.okaeri.commands.bukkit.type.CommandsBukkitTypes;
import eu.okaeri.commands.injector.CommandsInjector;
import eu.okaeri.commons.cache.CacheMap;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.schema.ConfigDeclaration;
import eu.okaeri.configs.schema.FieldDeclaration;
import eu.okaeri.i18n.configs.LocaleConfig;
import eu.okaeri.injector.Injectable;
import eu.okaeri.injector.Injector;
import eu.okaeri.injector.OkaeriInjector;
import eu.okaeri.injector.annotation.PostConstruct;
import eu.okaeri.placeholders.bukkit.BukkitPlaceholders;
import eu.okaeri.platform.bukkit.i18n.BI18n;
import eu.okaeri.platform.bukkit.i18n.I18nCommandsMessages;
import eu.okaeri.platform.bukkit.i18n.I18nCommandsTextHandler;
import eu.okaeri.platform.bukkit.i18n.I18nPrefixProvider;
import eu.okaeri.platform.core.component.ComponentHelper;
import eu.okaeri.platform.core.component.ExternalResourceProvider;
import eu.okaeri.platform.core.component.manifest.BeanManifest;
import eu.okaeri.platform.core.exception.BreakException;
import lombok.*;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.File;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.stream.Collectors;


public class OkaeriBukkitPlugin extends JavaPlugin {

    private static boolean useParallelism = Boolean.parseBoolean(System.getProperty("okaeri.platform.parallelism", "true"));

    private static final Map<Class<?>, Boolean> IS_UNSAFE_ASYNC_CACHE = new CacheMap<>();
    private static final Set<String> ASYNC_BANNED_TYPES = new HashSet<>(Arrays.asList(
            "org.bukkit.block.Block",
            "org.bukkit.Location",
            "org.bukkit.World"
    ));

    @SuppressWarnings("unchecked") private static final ExternalResourceProvider EXTERNAL_RESOURCE_PROVIDER = (name, type, source) -> {

        Class<? extends JavaPlugin> sourcePlugin = (Class<? extends JavaPlugin>) source;
        JavaPlugin plugin = JavaPlugin.getPlugin(sourcePlugin);

        if (plugin == null) {
            throw new BreakException("cannot provide external resource: " + name + ", " + type + " from " + source + ": cannot find source");
        }

        Injector externalInjector = ((OkaeriBukkitPlugin) plugin).getInjector();
        Optional<? extends Injectable<?>> injectable = externalInjector.getInjectable(name, type);

        if (!injectable.isPresent()) {
            throw new BreakException("cannot provide external resource: " + name + ", " + type + " from " + source + ": cannot find injectable");
        }

        return injectable.get().getObject();
    };

    @Getter private Injector injector;
    @Getter private Commands commands;
    @Getter private CommandsBukkit commandsBukkit;

    private BeanManifest beanManifest;
    private BukkitComponentCreator creator;
    private List<AsyncLoader> preloaders = Collections.synchronizedList(new ArrayList<>());

    public OkaeriBukkitPlugin() {
        this.postBukkitConstruct();
    }

    public OkaeriBukkitPlugin(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
        super(loader, description, dataFolder, file);
        this.postBukkitConstruct();
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
    private void postBukkitConstruct() {
        this.getLogger().info("Preloading " + this.getName() + " " + this.getDescription().getVersion());
        this.preloadData("Placeholders", () -> BukkitComponentCreator.defaultPlaceholders = BukkitPlaceholders.create(true));
        this.preloadData("BeanManifest", this::preloadManifest);
        this.preloadData("Config", this::preloadConfig);
        this.preloadData("LocaleConfig", this::preloadLocaleConfig);
    }

    private Thread createPreloadThread(@NonNull String name, @NonNull Runnable runnable) {
        Thread preloader = new Thread(runnable);
        preloader.setName("Okaeri Platform Preloader (" + this.getName() + ") - " + name);
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
        // commands
        this.commandsBukkit = CommandsBukkit.of(this).resultHandler(new BukkitCommandsResultHandler());
        this.commands = CommandsManager.create(CommandsInjector.of(this.commandsBukkit, this.injector)).register(new CommandsBukkitTypes());
        this.injector.registerInjectable("commands", this.commands);
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

            if (this.isUnsafeAsync(depend.getType())) {
                continue;
            }

            depend.setObject(this.creator.makeObject(depend, this.injector));
            this.injector.registerInjectable(depend.getName(), depend.getObject());
        }
    }

    private boolean isUnsafeAsync(@NonNull Class<?> configType) {

        Boolean cachedResult = IS_UNSAFE_ASYNC_CACHE.get(configType);
        if (cachedResult != null) {
            return cachedResult;
        }

        ConfigDeclaration declaration = ConfigDeclaration.of(configType);
        for (FieldDeclaration field : declaration.getFields()) {

            Class<?> fieldRealType = field.getType().getType();
            if (field.getType().isConfig()) {
                // subconfig - deep check
                if (!this.isUnsafeAsync(fieldRealType)) {
                    IS_UNSAFE_ASYNC_CACHE.put(configType, true);
                    return true;
                }
            }

            if (ASYNC_BANNED_TYPES.contains(fieldRealType.getCanonicalName())) {
                IS_UNSAFE_ASYNC_CACHE.put(configType, true);
                return true;
            }
        }

        IS_UNSAFE_ASYNC_CACHE.put(configType, false);
        return false;
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

        // apply i18n text resolver for commands framework
        Set<BI18n> i18nCommandsProviders = new HashSet<>();
        AtomicReference<I18nPrefixProvider> prefixProvider = new AtomicReference<>();
        this.injector.getInjectable("i18n", BI18n.class)
                .ifPresent(i18n -> {
                    BI18n bi18n = i18n.getObject();
                    prefixProvider.set(bi18n.getPrefixProvider());
                    i18nCommandsProviders.add(bi18n);
                });
        this.injector.getInjectable("i18n-platform-commands", BI18n.class)
                .ifPresent(i18n -> {
                    BI18n bi18n = i18n.getObject();
                    I18nPrefixProvider i18nPrefixProvider = prefixProvider.get();
                    if (i18nPrefixProvider != null) {
                        bi18n.setPrefixProvider(i18nPrefixProvider);
                    }
                    i18nCommandsProviders.add(bi18n);
                });
        this.commandsBukkit.textHandler(new I18nCommandsTextHandler(i18nCommandsProviders));

        // dispatch async logs
        // to show that these were loaded
        // before other components here
        if (this.creator != null) this.creator.dispatchLogs();

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
            // execute component tree and register everything
            this.beanManifest.execute(this.creator, this.injector, EXTERNAL_RESOURCE_PROVIDER);
            // sub-components do not require manual injecting because
            // these are filled at the initialization by the DI itself
            // plugin instance however is not, so here it goes
            ComponentHelper.injectComponentFields(this, this.injector);
            // call PostConstruct
            this.invokePostConstruct();
            // schedule onServerLoad
            this.getServer().getScheduler().runTaskLater(this, this::onServerLoad, 1);
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

    private void invokePostConstruct() {

        List<Method> postConstructs = Arrays.stream(this.getClass().getDeclaredMethods())
                .filter(method -> method.getAnnotation(PostConstruct.class) != null)
                .sorted(Comparator.comparingInt(method -> method.getAnnotation(PostConstruct.class).order()))
                .collect(Collectors.toList());

        for (Method postConstruct : postConstructs) {

            Object result = ComponentHelper.invokeMethod(this, postConstruct, this.injector);
            if (result == null) {
                continue;
            }

            this.injector.registerInjectable(postConstruct.getName(), result);
        }
    }

    @Override
    public void onDisable() {
        this.onPlatformDisabled();
    }

    public void onPlatformEnabled() {
    }

    public void onPlatformDisabled() {
    }

    public void onServerLoad() {
    }
}
