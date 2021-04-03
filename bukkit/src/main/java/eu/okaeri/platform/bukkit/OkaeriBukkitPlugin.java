package eu.okaeri.platform.bukkit;

import eu.okaeri.commands.CommandsManager;
import eu.okaeri.commands.OkaeriCommands;
import eu.okaeri.commands.bukkit.CommandsBukkit;
import eu.okaeri.commands.injector.CommandsInjector;
import eu.okaeri.commands.meta.ServiceMeta;
import eu.okaeri.commands.service.CommandService;
import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.postprocessor.SectionSeparator;
import eu.okaeri.configs.validator.okaeri.OkaeriValidator;
import eu.okaeri.configs.yaml.bukkit.YamlBukkitConfigurer;
import eu.okaeri.injector.Injectable;
import eu.okaeri.injector.Injector;
import eu.okaeri.injector.OkaeriInjector;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.platform.core.annotation.Bean;
import eu.okaeri.platform.core.annotation.Configuration;
import eu.okaeri.platform.core.annotation.WithBean;
import eu.okaeri.platform.core.exception.BreakException;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Collectors;


public class OkaeriBukkitPlugin extends JavaPlugin {

    private static final boolean VERBOSE = Boolean.getBoolean("okaeri.debug");

    @Getter private Injector injector;
    @Getter private OkaeriCommands commands;

    @Override
    public void onEnable() {
        this.postBukkitInitialize();
        this.onPlatformEnabled();
    }

    @Override
    public void onDisable() {
        this.onPlatformDisabled();
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    private void postBukkitInitialize() {

        // initialize DI and commands
        this.getLogger().info("Initializing " + this.getClass());
        this.injector = OkaeriInjector.create().registerInjectable(this);
        this.commands = CommandsManager.create(CommandsInjector.of(CommandsBukkit.of(this), this.injector));

        // load commands/other beans
        try {
            this.loadBeans(this, VERBOSE);
        } catch (BreakException exception) {
            this.getLogger().log(Level.SEVERE, "Stopping initialization, received break signal: " + exception.getMessage());
        }
    }

    private void loadBeans(Object object, boolean verbose) {

        Class<?> objectClazz = object.getClass();
        if (verbose) this.getLogger().info("Checking " + objectClazz);
        Method[] methods = objectClazz.getDeclaredMethods();
        if (verbose) this.getLogger().info("Methods: " + Arrays.stream(methods).map(Method::getName).collect(Collectors.joining(", ")));

        // check methods for bean initializers
        for (Method method : methods) {

            // check if bean
            Bean bean = method.getAnnotation(Bean.class);
            if (bean == null) {
                continue;
            }

            // gain access *hackerman*
            method.setAccessible(true);

            // read bean name and method params
            String beanName = bean.value();
            boolean register = bean.register();
            boolean scan = bean.scan();
            Parameter[] parameters = method.getParameters();

            // check for injectable parameters
            Object[] call = new Object[parameters.length];
            for (int i = 0; i < parameters.length; i++) {

                Parameter param = parameters[i];
                Class<?> paramType = param.getType();
                String name = (param.getAnnotation(Inject.class) != null) ? param.getAnnotation(Inject.class).value() : "";

                Optional<? extends Injectable<?>> injectable = this.injector.getInjectable(name, paramType);
                if (!injectable.isPresent()) {
                    throw new RuntimeException("Cannot create @Bean " + this.renderMethod(method) + ", no injectable of type " + paramType + " [" + name + "] found");
                }

                call[i] = paramType.cast(injectable.get().getObject());
            }

            // invoke bean creator
            Object result;
            try {
                result = method.invoke(object, call);
            } catch (Exception exception) {
                if (exception instanceof InvocationTargetException) {
                    if (exception.getCause() instanceof BreakException) {
                        throw (BreakException) exception.getCause();
                    }
                    throw new RuntimeException("Error creating @Bean " + this.renderMethod(method), exception.getCause());
                }
                throw new RuntimeException("Error creating @Bean " + this.renderMethod(method), exception);
            }

            // register bean
            this.registerBean(result, beanName, register, scan, method.getName(), method.getReturnType(), verbose);

            // self inject
            this.selfInjectBean(object);
        }

        // check for @WithBean annotation
        WithBean.List withBeanListAnnotation = objectClazz.getAnnotation(WithBean.List.class);
        WithBean withBeanAnnotation = objectClazz.getAnnotation(WithBean.class);
        List<WithBean> withBeanList = (withBeanListAnnotation != null)
                ? Arrays.asList(withBeanListAnnotation.value())
                : (((withBeanAnnotation) == null)
                ? Collections.emptyList()
                : Collections.singletonList(withBeanAnnotation));
        // register all beans
        for (WithBean withBean : withBeanList) {

            // create instance using DI provider or pass class if config
            Class<?> beanClazz = withBean.value();
            Object beanObject = OkaeriConfig.class.isAssignableFrom(beanClazz)
                    ? beanClazz : this.injector.createInstance(beanClazz);

            // register bean
            String debugName = "@WithBean from " + objectClazz.getSimpleName();
            this.registerBean(beanObject, "", withBean.register(), withBean.scan(), debugName, beanClazz, verbose);

            // self inject
            this.selfInjectBean(object);
        }
    }

    @SuppressWarnings("unchecked")
    private void registerBean(Object beanObject, String beanName, boolean register, boolean scan, String debugName, Class<?> beanType, boolean verbose) {

        if (verbose) {
            String beanInfo = "name=" + beanName + ",register=" + register + ",scan=" + scan;
            this.getLogger().info("Created @Bean(" + beanInfo + ") " + debugName + " = " + beanType);
        }

        // register if config class
        if (register && (beanObject instanceof Class) && (OkaeriConfig.class.isAssignableFrom((Class<?>) beanObject))) {

            Class<? extends OkaeriConfig> beanClazz = (Class<? extends OkaeriConfig>) beanObject;
            Configuration configuration = beanClazz.getAnnotation(Configuration.class);
            if (configuration == null) {
                throw new IllegalArgumentException("Cannot auto-register OkaeriConfig without @Configuration annotation, use register=false to just register an instance");
            }

            String path = configuration.path();
            boolean defaultNotNull = configuration.defaultNotNull();

            try {
                OkaeriConfig config = ConfigManager.create(beanClazz, (it) -> {
                    it.withBindFile(new File(this.getDataFolder(), path));
                    it.withConfigurer(new OkaeriValidator(new YamlBukkitConfigurer(SectionSeparator.NONE), defaultNotNull));
                    it.saveDefaults();
                    it.load(true);
                });
                beanObject = beanClazz.cast(config);
                this.getLogger().info("- Loaded configuration: " + path);
            }
            catch (Exception exception) {
                this.getLogger().log(Level.SEVERE, "Failed to load configuration " + path, exception);
                this.getServer().getPluginManager().disablePlugin(this);
                throw new RuntimeException("Configuration load failure");
            }
        }

        // register if command
        if (register && (beanObject instanceof CommandService)) {

            CommandService commandService = (CommandService) beanObject;
            ServiceMeta serviceMeta = ServiceMeta.of(commandService);
            this.commands.register(commandService);

            String metaString = "";
            if (!serviceMeta.getAliases().isEmpty()) {
                metaString += " [aliases: " + String.join(", ", serviceMeta.getAliases()) + "]";
            }

            if (!serviceMeta.getDescription().isEmpty()) {
                metaString += " [description: " + serviceMeta.getDescription() + "]";
            }

            beanObject = commandService;
            this.getLogger().info("- Added command: " + serviceMeta.getLabel() + metaString);
        }

        // register if listener
        if (register && (beanObject instanceof Listener)) {

            Listener listener = (Listener) beanObject;
            this.getServer().getPluginManager().registerEvents(listener, this);

            beanObject = listener;
            this.getLogger().info("- Added listener");
        }

        // save injectable
        this.registerInjectableAndSelfInject(beanName, beanObject);

        // scan subbeans
        if (scan) {
            this.loadBeans(beanObject, verbose);
        }
    }

    private void registerInjectableAndSelfInject(String beanName, Object beanObject) {
        if (beanObject == null) return;
        String beanMeta = (beanName.isEmpty() ? "~unnamed~" : beanName);
        if (VERBOSE) this.getLogger().info("#registerInjectableAndSelfInject (" + beanMeta + ", " + beanObject.getClass().getSimpleName() + ")");
        this.injector.registerInjectable(beanName, beanObject);
        this.selfInjectBean(beanObject);
    }

    @SneakyThrows
    private void selfInjectBean(Object bean) {

        if (bean == null) return;
        Class<?> beanClazz = bean.getClass();
        Field[] fields = beanClazz.getDeclaredFields();

        for (Field field : fields) {

            Inject inject = field.getAnnotation(Inject.class);
            if (inject == null) {
                continue;
            }

            Optional<? extends Injectable<?>> injectable = this.injector.getInjectable(inject.value(), field.getType());
            if (!injectable.isPresent()) {
                continue;
            }

            Injectable<?> injectObject = injectable.get();
            field.setAccessible(true);
            field.set(bean, injectObject.getObject());
        }
    }

    private String renderMethod(Method method) {
        return  method.getReturnType().getSimpleName() + "->" + method.getName()
                + "(" + Arrays.stream(method.getParameters())
                .map(parameter -> parameter.getType().getSimpleName())
                .collect(Collectors.joining(", ")) + ")";
    }

    public void onPlatformEnabled() {
    }

    public void onPlatformDisabled() {
    }
}
