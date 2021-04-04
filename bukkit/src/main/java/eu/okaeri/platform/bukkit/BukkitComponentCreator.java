package eu.okaeri.platform.bukkit;

import eu.okaeri.commands.OkaeriCommands;
import eu.okaeri.commands.meta.ServiceMeta;
import eu.okaeri.commands.service.CommandService;
import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.postprocessor.SectionSeparator;
import eu.okaeri.configs.validator.okaeri.OkaeriValidator;
import eu.okaeri.configs.yaml.bukkit.YamlBukkitConfigurer;
import eu.okaeri.injector.Injector;
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
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class BukkitComponentCreator implements ComponentCreator {

    private final JavaPlugin plugin;
    private final OkaeriCommands commands;

    @Getter private List<OkaeriConfig> loadedConfigs;
    @Getter private List<CommandService> loadedCommands;
    @Getter private List<Listener> loadedListeners;

    @Override
    public boolean isComponent(Class<?> type) {
        return (type.getAnnotation(Component.class) != null)
                || (type.getAnnotation(Configuration.class) != null)
                || CommandService.class.isAssignableFrom(type)
                || OkaeriBukkitPlugin.class.isAssignableFrom(type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object makeObject(BeanManifest manifest, Injector injector) {

        // validation
        if (!Arrays.asList(BeanSource.METHOD, BeanSource.COMPONENT).contains(manifest.getSource())) {
            throw new RuntimeException("Cannot transform from source " + manifest.getSource());
        }

        boolean register = manifest.isRegister();
        Object beanObject = manifest.getObject();
        Class<?> manifestType = manifest.getType();
        boolean changed = false;

        // create config instance if applicable
        if (register && (OkaeriConfig.class.isAssignableFrom(manifestType))) {

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
                beanObject = config;;
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
                beanObject = ComponentHelper.invokeMethod(manifest.getParent(), manifest.getMethod(), injector);
            } else if (manifest.getSource() == BeanSource.COMPONENT) {
                if (!this.isComponent(manifestType)) {
                    throw new RuntimeException("Cannot create instance of non-component class " + manifestType);
                }
                beanObject = injector.createInstance(manifestType);
            }
        }

        // register if command
        if (register && (beanObject instanceof CommandService)) {

            CommandService commandService = (CommandService) beanObject;
            ServiceMeta serviceMeta = ServiceMeta.of(commandService);
            this.commands.register(commandService);

            Map<String, String> commandMeta = new LinkedHashMap<>();
            commandMeta.put("label", serviceMeta.getLabel());
            if (!serviceMeta.getAliases().isEmpty()) commandMeta.put("aliases", "[" + String.join(", ", serviceMeta.getAliases()) + "]");
            if (!serviceMeta.getDescription().isEmpty()) commandMeta.put("description", serviceMeta.getDescription());

            beanObject = commandService;
            this.loadedCommands.add(commandService);

            String commandMetaString = commandMeta.entrySet().stream()
                    .map(entry -> entry.getKey() + " = " + entry.getValue())
                    .collect(Collectors.joining(", "));

            this.plugin.getLogger().info("- Added command: " + commandService.getClass().getSimpleName() + " { " + commandMetaString + " }");
        }

        // register if listener
        if (register && (beanObject instanceof Listener)) {

            Listener listener = (Listener) beanObject;
            this.plugin.getServer().getPluginManager().registerEvents(listener, this.plugin);

            beanObject = listener;
            this.loadedListeners.add(listener);

            String listenerMethods = Arrays.stream(listener.getClass().getDeclaredMethods())
                    .filter(method -> method.getAnnotation(EventHandler.class) != null)
                    .map(Method::getName)
                    .collect(Collectors.joining(", "));

            this.plugin.getLogger().info("- Added listener: " + listener.getClass().getSimpleName() + " { " + listenerMethods + " }");
        }

        // inject
        ComponentHelper.injectComponentFields(beanObject, injector);
        return beanObject;
    }
}
