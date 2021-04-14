package eu.okaeri.platform.bukkit;

import eu.okaeri.commands.Commands;
import eu.okaeri.commands.meta.ServiceMeta;
import eu.okaeri.commands.service.CommandService;
import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.configurer.Configurer;
import eu.okaeri.configs.serdes.OkaeriSerdesPack;
import eu.okaeri.configs.serdes.commons.SerdesCommons;
import eu.okaeri.configs.validator.okaeri.OkaeriValidator;
import eu.okaeri.configs.yaml.bukkit.YamlBukkitConfigurer;
import eu.okaeri.configs.yaml.bukkit.serdes.SerdesBukkit;
import eu.okaeri.i18n.configs.LocaleConfig;
import eu.okaeri.i18n.configs.LocaleConfigManager;
import eu.okaeri.injector.Injector;
import eu.okaeri.placeholders.Placeholders;
import eu.okaeri.placeholders.bukkit.BukkitPlaceholders;
import eu.okaeri.platform.bukkit.annotation.Timer;
import eu.okaeri.platform.bukkit.commons.i18n.BI18n;
import eu.okaeri.platform.bukkit.commons.i18n.I18nColorsConfig;
import eu.okaeri.platform.bukkit.commons.i18n.PlayerLocaleProvider;
import eu.okaeri.platform.core.annotation.Bean;
import eu.okaeri.platform.core.annotation.Component;
import eu.okaeri.platform.core.annotation.Configuration;
import eu.okaeri.platform.core.annotation.Messages;
import eu.okaeri.platform.core.component.ComponentCreator;
import eu.okaeri.platform.core.component.ComponentHelper;
import eu.okaeri.platform.core.component.manifest.BeanManifest;
import eu.okaeri.platform.core.component.manifest.BeanSource;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static eu.okaeri.platform.core.component.ComponentHelper.invokeMethod;

@RequiredArgsConstructor
public class BukkitComponentCreator implements ComponentCreator {

    public static Placeholders defaultPlaceholders;

    private final OkaeriBukkitPlugin plugin;
    private final Commands commands;
    private final Injector injector;

    @Getter private List<OkaeriConfig> loadedConfigs = new ArrayList<>();
    @Getter private List<LocaleConfig> loadedLocaleConfigs = new ArrayList<>();
    @Getter private List<CommandService> loadedCommands = new ArrayList<>();
    @Getter private List<Listener> loadedListeners = new ArrayList<>();
    @Getter private List<BukkitTask> loadedTimers = new ArrayList<>();
    @Getter private List<String> asyncLogs = new ArrayList<>();

    public void dispatchLogs() {
        this.asyncLogs.stream()
                .flatMap(asyncLog -> Arrays.stream(asyncLog.split("\n")))
                .forEach(line -> this.plugin.getLogger().info(line));
    }

    private void log(String message) {
        if (Bukkit.isPrimaryThread()) {
            Arrays.stream(("- " + message).split("\n"))
                    .forEach(line -> this.plugin.getLogger().info(line));
            return;
        }
        this.asyncLogs.add("~ " + message);
    }

    @Override
    public boolean isComponent(Class<?> type) {
        return (type.getAnnotation(Component.class) != null)
                || (type.getAnnotation(Timer.class) != null)
                || (type.getAnnotation(Configuration.class) != null)
                || (type.getAnnotation(Messages.class) != null)
                || CommandService.class.isAssignableFrom(type)
                || OkaeriBukkitPlugin.class.isAssignableFrom(type);
    }

    @Override
    public boolean isComponentMethod(Method method) {
        return (method.getAnnotation(Bean.class) != null)
                || (method.getAnnotation(Timer.class) != null);
    }

    @Override
    @SuppressWarnings({"unchecked", "ResultOfMethodCallIgnored"})
    public Object makeObject(BeanManifest manifest, Injector injector) {

        // validation
        if (!Arrays.asList(BeanSource.METHOD, BeanSource.COMPONENT).contains(manifest.getSource())) {
            throw new RuntimeException("Cannot transform from source " + manifest.getSource());
        }

        Timer timer = null;
        String timerName = null;
        boolean register = manifest.isRegister();
        Object beanObject = manifest.getObject();
        Class<?> manifestType = manifest.getType();
        boolean changed = false;

        // create config instance if applicable - @Register only - allows method component (manually created config) to be processed
        if (register && (!LocaleConfig.class.isAssignableFrom(manifestType)) && (OkaeriConfig.class.isAssignableFrom(manifestType)) && (manifest.getSource() == BeanSource.COMPONENT)) {

            long start = System.currentTimeMillis();
            Class<? extends OkaeriConfig> beanClazz = (Class<? extends OkaeriConfig>) manifestType;
            Configuration configuration = beanClazz.getAnnotation(Configuration.class);
            if (configuration == null) {
                throw new IllegalArgumentException("Cannot auto-register OkaeriConfig without @Configuration annotation, use register=false to just register as instance: " + beanClazz);
            }

            String path = configuration.path();
            boolean defaultNotNull = configuration.defaultNotNull();
            Class<? extends Configurer> provider = configuration.provider();

            try {
                Configurer configurer = (provider == Configuration.DEFAULT.class)
                        ? new YamlBukkitConfigurer()
                        : this.injector.createInstance(provider);

                OkaeriSerdesPack[] serdesPacks = Stream.concat(Stream.of(SerdesBukkit.class, SerdesCommons.class), Arrays.stream(configuration.serdes()))
                        .map(this.injector::createInstance)
                        .toArray(OkaeriSerdesPack[]::new);

                OkaeriConfig config = ConfigManager.create(beanClazz, (it) -> {
                    it.withBindFile(new File(this.plugin.getDataFolder(), path));
                    it.withConfigurer(new OkaeriValidator(configurer, defaultNotNull), serdesPacks);
                    it.saveDefaults();
                    it.load(true);
                });

                long took = System.currentTimeMillis() - start;
                this.log("Loaded configuration: " + beanClazz.getSimpleName() + " { path = " + path + ", provider = " + provider.getSimpleName() + " } [" + took + " ms]");
                this.loadedConfigs.add(config);
                beanObject = config;
                changed = true;
            }
            catch (Exception exception) {
                this.plugin.getLogger().log(Level.SEVERE, "Failed to load configuration " + path, exception);
                this.plugin.getServer().getPluginManager().disablePlugin(this.plugin);
                throw new RuntimeException("Configuration load failure");
            }
        }

        // create locale config instance if applicable - @Register only - allows method component (manually created config) to be processed
        if (register && (LocaleConfig.class.isAssignableFrom(manifestType)) && (manifest.getSource() == BeanSource.COMPONENT)) {

            // not really needed thanks to preloading in OkaeriBukkitPlugin but just in case
            if (defaultPlaceholders == null) {
                long start = System.currentTimeMillis();
                defaultPlaceholders = BukkitPlaceholders.create(true);
                long took = System.currentTimeMillis() - start;
                int resolversCount = defaultPlaceholders.getResolversCount();
                this.log("Loaded placeholders system for i18n { resolversCount = " + resolversCount + " } [" + took + " ms]");
            }

            long start = System.currentTimeMillis();
            Class<? extends LocaleConfig> beanClazz = (Class<? extends LocaleConfig>) manifestType;
            Messages messages = beanClazz.getAnnotation(Messages.class);
            if (messages == null) {
                throw new IllegalArgumentException("Cannot auto-register LocaleConfig without @Messages annotation, use register=false to just register as instance: " + beanClazz);
            }

            String path = messages.path();
            String suffix = messages.suffix();
            Class<? extends Configurer> provider = messages.provider();
            Locale defaultLocale = Locale.forLanguageTag(messages.defaultLocale());
            boolean unpack = messages.unpack();
            File directory = new File(this.plugin.getDataFolder(), path);
            boolean directoryExisted = directory.exists();
            directory.mkdirs();

            // unpack files from the resources
            if (unpack && !directoryExisted) {
                try {
                    Method getFile = JavaPlugin.class.getDeclaredMethod("getFile");
                    getFile.setAccessible(true);
                    File jar = (File) getFile.invoke(this.plugin);
                    JarFile jarFile = new JarFile(jar);
                    Enumeration<JarEntry> entries = jarFile.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry jarEntry = entries.nextElement();
                        String entryName = jarEntry.getName();
                        if (!entryName.startsWith(path + "/")) {
                            continue;
                        }
                        if (!jarEntry.isDirectory()) {
                            continue;
                        }
                        File file = new File(this.plugin.getDataFolder(), entryName);
                        if (file.exists()) {
                            continue;
                        }
                        InputStream is = jarFile.getInputStream(jarEntry);
                        FileOutputStream fos = new FileOutputStream(file);
                        while (is.available() > 0) {
                            fos.write(is.read());
                        }
                        fos.close();
                        is.close();
                    }
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | IOException exception) {
                    this.plugin.getLogger().log(Level.SEVERE, "Failed to unpack resources", exception);
                    exception.printStackTrace();
                }
            }

            // gather colors config
            I18nColorsConfig colorsConfig = ConfigManager.create(I18nColorsConfig.class, (it) -> {
                Configurer configurer = (provider == Messages.DEFAULT.class) ? new YamlBukkitConfigurer() : this.injector.createInstance(provider);
                it.withConfigurer(configurer, new SerdesCommons());
                it.withBindFile(new File(directory, "colors" + suffix));
                if (it.getBindFile().exists()) it.load(true);
                if (unpack && !directoryExisted) it.saveDefaults();
            });

            // load locales
            try {
                LocaleConfig template = LocaleConfigManager.createTemplate(beanClazz);
                File[] files = directory.listFiles((dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(suffix));
                if (files == null) files = new File[0];

                BI18n i18n = new BI18n(colorsConfig);
                i18n.setDefaultLocale(defaultLocale);
                i18n.registerLocaleProvider(new PlayerLocaleProvider());
                i18n.setPlaceholders(defaultPlaceholders.copy());

                List<Locale> loadedLocales = new ArrayList<>();
                this.injector.registerInjectable(path, template);

                // check path directory for locale files
                for (File file : files) {
                    // read locale from name
                    String name = file.getName();
                    String localeName = name.substring(0, name.length() - suffix.length());
                    Locale locale = Locale.forLanguageTag(localeName);
                    // create configurer
                    Configurer configurer = (provider == Messages.DEFAULT.class)
                            ? new YamlBukkitConfigurer()
                            : this.injector.createInstance(provider);
                    // register
                    LocaleConfig localeConfig = LocaleConfigManager.create(beanClazz, configurer, file, !defaultLocale.equals(locale));
                    i18n.registerConfig(locale, localeConfig);
                    this.loadedLocaleConfigs.add(localeConfig);
                    loadedLocales.add(locale);
                }

                // default locale was not overwritten by a file
                if (!loadedLocales.contains(defaultLocale)) {
                    // create configurer
                    Configurer configurer = (provider == Messages.DEFAULT.class)
                            ? new YamlBukkitConfigurer()
                            : this.injector.createInstance(provider);
                    // register default locale based on interface values
                    LocaleConfig defaultLocaleConfig = ConfigManager.create(beanClazz, it -> {
                        it.withBindFile(new File(directory, messages.defaultLocale() + suffix));
                        it.withConfigurer(configurer);
                        if (unpack && !directoryExisted) it.saveDefaults();
                    });
                    i18n.registerConfig(defaultLocale, defaultLocaleConfig);
                    this.loadedLocaleConfigs.add(defaultLocaleConfig);
                    loadedLocales.add(defaultLocale);
                }

                long took = System.currentTimeMillis() - start;
                this.log("Loaded messages: " + beanClazz.getSimpleName() + " { path = " + path + ", suffix = " + suffix + ", provider = " + provider.getSimpleName() + " } [" + took + " ms]\n" +
                        "  > " + loadedLocales.stream().map(Locale::toString).collect(Collectors.joining(", ")));
                beanObject = i18n;
                changed = true;
            }
            catch (Exception exception) {
                this.plugin.getLogger().log(Level.SEVERE, "Failed to load messages configuration " + path, exception);
                this.plugin.getServer().getPluginManager().disablePlugin(this.plugin);
                throw new RuntimeException("Messages configuration load failure");
            }
        }

        // create instance generic way
        if (!changed) {
            if (manifest.getSource() == BeanSource.METHOD) {
                // register method timer - skip beans creating Runnable but save Timer (as override)
                timer = manifest.getMethod().getAnnotation(Timer.class);
                if (register && (timer != null) && !Runnable.class.isAssignableFrom(manifest.getType())) {
                    beanObject = (Runnable) () -> invokeMethod(manifest, injector); // create runnable from method
                    timerName = manifest.getMethod().getName(); // BukkitComponentCreator$$Lambda$754/0x0000000800b52c40 does not look useful in init messages
                }
                // normal bean
                else {
                    beanObject = invokeMethod(manifest, injector);
                }
            } else if (manifest.getSource() == BeanSource.COMPONENT) {
                if (!this.isComponent(manifestType)) {
                    throw new RuntimeException("Cannot create instance of non-component class " + manifestType);
                }
                beanObject = injector.createInstance(manifestType);
            }
        }

        // register component timer
        if (register) {
            // not originating from method, update annotation
            if (timer == null) {
                timer = beanObject.getClass().getAnnotation(Timer.class);
            }
            // finally, register
            if ((timer != null) && (beanObject instanceof Runnable)) {
                manifest.setName(timer.name());
                this.registerTimer(timer, (Runnable) beanObject, timerName);
            }
        }

        // register if command - works for beans created by method too
        if (register && (beanObject instanceof CommandService)) {
            this.registerCommand((CommandService) beanObject);
        }

        // register if listener - works for beans created by method too
        if (register && (beanObject instanceof Listener)) {
            this.registerListener((Listener) beanObject);
        }

        // inject
        ComponentHelper.injectComponentFields(beanObject, injector);
        return beanObject;
    }

    private void registerListener(Listener listener) {

        this.plugin.getServer().getPluginManager().registerEvents(listener, this.plugin);
        this.loadedListeners.add(listener);

        String listenerMethods = Arrays.stream(listener.getClass().getDeclaredMethods())
                .filter(method -> method.getAnnotation(EventHandler.class) != null)
                .map(Method::getName)
                .collect(Collectors.joining(", "));

        this.log("Added listener: " + listener.getClass().getSimpleName() + " { " + listenerMethods + " }");
    }

    private void registerCommand(CommandService commandService) {

        if (this.commands == null) {
            throw new IllegalArgumentException("cannot register command service with commands provider being null");
        }

        ServiceMeta serviceMeta = ServiceMeta.of(commandService);
        this.commands.getRegistry().register(commandService);

        Map<String, String> commandMeta = new LinkedHashMap<>();
        commandMeta.put("label", serviceMeta.getLabel());
        if (!serviceMeta.getAliases().isEmpty()) commandMeta.put("aliases", "[" + String.join(", ", serviceMeta.getAliases()) + "]");
        if (!serviceMeta.getDescription().isEmpty()) commandMeta.put("description", serviceMeta.getDescription());

        this.loadedCommands.add(commandService);

        String commandMetaString = commandMeta.entrySet().stream()
                .map(entry -> entry.getKey() + " = " + entry.getValue())
                .collect(Collectors.joining(", "));

        this.log("Added command: " + commandService.getClass().getSimpleName() + " { " + commandMetaString + " }");
    }

    private void registerTimer(Timer timer, Runnable runnable, String nameOverride) {

        int delay = (timer.delay() == -1) ? timer.rate() : timer.delay();
        BukkitScheduler scheduler = this.plugin.getServer().getScheduler();

        BukkitTask task = timer.async()
                ? scheduler.runTaskTimerAsynchronously(this.plugin, runnable, delay, timer.rate())
                : scheduler.runTaskTimer(this.plugin, runnable, delay, timer.rate());

        this.loadedTimers.add(task);

        String resultingTimerName = ((nameOverride == null) ? runnable.getClass().getSimpleName() : nameOverride);
        String timerMeta = "delay = " + delay + ", rate = " + timer.rate() + ", async = " + timer.async();
        this.log("Added timer: " + resultingTimerName + " { " + timerMeta + " }");
    }
}
