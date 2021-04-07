package eu.okaeri.platform.bukkit;

import eu.okaeri.commands.Commands;
import eu.okaeri.commands.meta.ServiceMeta;
import eu.okaeri.commands.service.CommandService;
import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.postprocessor.SectionSeparator;
import eu.okaeri.configs.validator.okaeri.OkaeriValidator;
import eu.okaeri.configs.yaml.bukkit.YamlBukkitConfigurer;
import eu.okaeri.injector.Injector;
import eu.okaeri.platform.bukkit.annotation.Timer;
import eu.okaeri.platform.core.annotation.Bean;
import eu.okaeri.platform.core.annotation.Component;
import eu.okaeri.platform.core.annotation.Configuration;
import eu.okaeri.platform.core.component.ComponentCreator;
import eu.okaeri.platform.core.component.ComponentHelper;
import eu.okaeri.platform.core.component.manifest.BeanManifest;
import eu.okaeri.platform.core.component.manifest.BeanSource;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static eu.okaeri.platform.core.component.ComponentHelper.invokeMethod;

@RequiredArgsConstructor
public class BukkitComponentCreator implements ComponentCreator {

    private final Plugin plugin;
    private final Commands commands;

    @Getter private List<OkaeriConfig> loadedConfigs = new ArrayList<>();
    @Getter private List<CommandService> loadedCommands = new ArrayList<>();
    @Getter private List<Listener> loadedListeners = new ArrayList<>();
    @Getter private List<BukkitTask> loadedTimers = new ArrayList<>();

    @Override
    public boolean isComponent(Class<?> type) {
        return (type.getAnnotation(Component.class) != null)
                || (type.getAnnotation(Timer.class) != null)
                || (type.getAnnotation(Configuration.class) != null)
                || CommandService.class.isAssignableFrom(type)
                || OkaeriBukkitPlugin.class.isAssignableFrom(type);
    }

    @Override
    public boolean isComponentMethod(Method method) {
        return (method.getAnnotation(Bean.class) != null)
                || (method.getAnnotation(Timer.class) != null);
    }

    @Override
    @SuppressWarnings("unchecked")
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
        if (register && (OkaeriConfig.class.isAssignableFrom(manifestType)) && (manifest.getSource() == BeanSource.COMPONENT)) {

            Class<? extends OkaeriConfig> beanClazz = (Class<? extends OkaeriConfig>) manifestType;
            Configuration configuration = beanClazz.getAnnotation(Configuration.class);
            if (configuration == null) {
                throw new IllegalArgumentException("Cannot auto-register OkaeriConfig without @Configuration annotation, use register=false to just register an instance");
            }

            String path = configuration.path();
            boolean defaultNotNull = configuration.defaultNotNull();

            try {
                OkaeriConfig config = ConfigManager.create(beanClazz, (it) -> {
                    it.withBindFile(new File(this.plugin.getDataFolder(), path));
                    it.withConfigurer(new OkaeriValidator(new YamlBukkitConfigurer(SectionSeparator.NONE), defaultNotNull));
                    it.saveDefaults();
                    it.load(true);
                });
                this.plugin.getLogger().info("- Loaded configuration: " + beanClazz.getSimpleName() + " { path = " + path + " }");
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
            }
            else if (manifest.getSource() == BeanSource.COMPONENT) {
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

        this.plugin.getLogger().info("- Added listener: " + listener.getClass().getSimpleName() + " { " + listenerMethods + " }");
    }

    private void registerCommand(CommandService commandService) {

        ServiceMeta serviceMeta = ServiceMeta.of(commandService);
        this.commands.register(commandService);

        Map<String, String> commandMeta = new LinkedHashMap<>();
        commandMeta.put("label", serviceMeta.getLabel());
        if (!serviceMeta.getAliases().isEmpty()) commandMeta.put("aliases", "[" + String.join(", ", serviceMeta.getAliases()) + "]");
        if (!serviceMeta.getDescription().isEmpty()) commandMeta.put("description", serviceMeta.getDescription());

        this.loadedCommands.add(commandService);

        String commandMetaString = commandMeta.entrySet().stream()
                .map(entry -> entry.getKey() + " = " + entry.getValue())
                .collect(Collectors.joining(", "));

        this.plugin.getLogger().info("- Added command: " + commandService.getClass().getSimpleName() + " { " + commandMetaString + " }");
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
        this.plugin.getLogger().info("- Added timer: " + resultingTimerName + " { " + timerMeta + " }");
    }
}
