package eu.okaeri.platform.bukkit;

import eu.okaeri.commands.CommandsManager;
import eu.okaeri.commands.OkaeriCommands;
import eu.okaeri.commands.bukkit.CommandsBukkit;
import eu.okaeri.commands.injector.CommandsInjector;
import eu.okaeri.commands.meta.ServiceMeta;
import eu.okaeri.commands.service.CommandService;
import eu.okaeri.injector.Injectable;
import eu.okaeri.injector.Injector;
import eu.okaeri.injector.OkaeriInjector;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.platform.bukkit.annotation.Bean;
import eu.okaeri.platform.bukkit.annotation.WithBean;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;


public class OkaeriPlugin extends JavaPlugin {

    @Getter private Injector injector;
    @Getter private OkaeriCommands commands;

    private List<Listener> awaitingListeners = new ArrayList<>();
    private List<CommandService> awaitingCommands = new ArrayList<>();

    public OkaeriPlugin() {
        super();
        this.postBukkitInitialize();
    }

    public OkaeriPlugin(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
        super(loader, description, dataFolder, file);
        this.postBukkitInitialize();
    }

    @Override
    public void onEnable() {
        this.awaitingCommands.forEach(this.commands::register);
        this.awaitingListeners.forEach(listener -> this.getServer().getPluginManager().registerEvents(listener, this));
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
        this.postBukkitLoadBeans(this);
    }

    private void postBukkitLoadBeans(Object object) {

        Class<?> objectClazz = object.getClass();
        this.getLogger().info("Checking " + objectClazz);
        Method[] methods = objectClazz.getDeclaredMethods();

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
            Parameter[] parameters = method.getParameters();

            // check for injectable parameters
            Object[] call = new Object[parameters.length];
            for (int i = 0; i < parameters.length; i++) {

                Parameter param = parameters[i];
                Class<?> paramType = param.getType();
                String name = (param.getAnnotation(Inject.class) != null) ? param.getAnnotation(Inject.class).value() : "";

                Optional<? extends Injectable<?>> injectable = this.injector.getInjectable(name, paramType);
                if (!injectable.isPresent()) {
                    throw new RuntimeException("Cannot create @Bean " + method + ", no injectable of type " + paramType + " [" + name + "] found");
                }

                call[i] = paramType.cast(injectable.get().getObject());
            }

            // invoke bean creator
            Object result;
            try {
                result = method.invoke(object, call);
            } catch (Exception exception) {
                if (exception instanceof InvocationTargetException) {
                    throw new RuntimeException("Error creating @Bean " + method, exception.getCause());
                }
                throw new RuntimeException("Error creating @Bean " + method, exception);
            }

            // register bean
            this.postBukkitRegisterBean(result, beanName, register, method.getName(), method.getReturnType());
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

            // create instance using DI provider
            Class<?> beanClazz = withBean.value();
            Object instance = this.injector.createInstance(beanClazz);

            // register bean
            String debugName = "@WithBean from " + objectClazz;
            this.postBukkitRegisterBean(instance, "", withBean.register(), debugName, beanClazz);
        }
    }

    private void postBukkitRegisterBean(Object beanObject, String beanName, boolean register, String debugName, Class<?> beanType) {

        this.injector.registerInjectable(beanName, beanObject);
        this.getLogger().info("Created @Bean(" + (beanName.isEmpty() ? "~unnamed~" : beanName) + ", " + register + ") " + debugName + " = " + beanType);

        // register if command
        if (register && (beanObject instanceof CommandService)) {

            CommandService commandService = (CommandService) beanObject;
            ServiceMeta serviceMeta = ServiceMeta.of(commandService);
            this.awaitingCommands.add(commandService);

            String metaString = "";
            if (!serviceMeta.getAliases().isEmpty()) {
                metaString += " [aliases: " + String.join(", ", serviceMeta.getAliases()) + "]";
            }

            if (!serviceMeta.getDescription().isEmpty()) {
                metaString += " [description: " + serviceMeta.getDescription() + "]";
            }

            this.getLogger().info("- Added command: " + serviceMeta.getLabel() + metaString);
        }

        // register if listener
        if (register && (beanObject instanceof Listener)) {

            Listener listener = (Listener) beanObject;
            this.awaitingListeners.add(listener);

            this.getLogger().info("- Added listener");
        }

        // register subbeans
        if (register) {
            this.postBukkitLoadBeans(beanObject);
        }
    }

    public void onPlatformEnabled() {
    }

    public void onPlatformDisabled() {
    }
}
