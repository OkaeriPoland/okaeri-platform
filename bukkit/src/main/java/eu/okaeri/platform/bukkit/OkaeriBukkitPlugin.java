package eu.okaeri.platform.bukkit;

import eu.okaeri.commands.bukkit.CommandsBukkit;
import eu.okaeri.injector.Injector;
import eu.okaeri.platform.bukkit.commands.BukkitCommandsResultHandler;
import eu.okaeri.platform.bukkit.component.BukkitComponentCreator;
import eu.okaeri.platform.bukkit.component.BukkitCreatorRegistry;
import eu.okaeri.platform.bukkit.plan.*;
import eu.okaeri.platform.core.OkaeriPlatform;
import eu.okaeri.platform.core.component.ComponentHelper;
import eu.okaeri.platform.core.component.creator.ComponentCreator;
import eu.okaeri.platform.core.plan.ExecutionPlan;
import eu.okaeri.platform.core.plan.ExecutionResult;
import eu.okaeri.platform.core.plan.task.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.File;
import java.util.Arrays;

import static eu.okaeri.platform.core.plan.ExecutionPhase.*;


public class OkaeriBukkitPlugin extends JavaPlugin implements OkaeriPlatform {

    @Getter private final File file = ComponentHelper.getJarFile(OkaeriBukkitPlugin.class);
    @Getter @Setter private Injector injector;
    @Getter @Setter private ComponentCreator creator;

    public OkaeriBukkitPlugin() {
        super();
    }

    protected OkaeriBukkitPlugin(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
        super(loader, description, dataFolder, file);
    }

    @Override
    public void log(@NonNull String message) {
        this.getLogger().info(message);
    }

    @Override
    public void plan(@NonNull ExecutionPlan plan) {

        plan.add(PRE_SETUP, new InjectorSetupTask());
        plan.add(PRE_SETUP, new BukkitPlaceholdersSetupTask());
        plan.add(PRE_SETUP, new BukkitInjectablesSetupTask());
        plan.add(PRE_SETUP, new CommandsSetupTask(CommandsBukkit.of(this).resultHandler(new BukkitCommandsResultHandler())));
        plan.add(PRE_SETUP, new CreatorSetupTask(BukkitComponentCreator.class, BukkitCreatorRegistry.class));

        plan.add(POST_SETUP, new BukkitExternalResourceProviderSetupTask());
        plan.add(POST_SETUP, new BeanManifestCreateTask());
        plan.add(POST_SETUP, new BukkitCommandsI18nManifestTask());
        plan.add(POST_SETUP, new BeanManifestExecuteTask());
        plan.add(POST_SETUP, new BukkitCommandsI18nSetupTask());

        plan.add(SHUTDOWN, new PersistenceShutdownTask());
    }

    private ExecutionPlan plan;

    @Override
    @Deprecated
    public void onEnable() {
        // execute using plan
        this.log("Loading " + this.getClass().getSimpleName());
        ExecutionResult result = ExecutionPlan.dispatch(this);
        this.log(this.getCreator().getSummaryText(result.getTotalMillis()));
        this.plan = result.getPlan();
        // compatibility
        this.onPlatformEnable();
    }

    @Override
    @Deprecated
    public void onDisable() {
        // call shutdown hooks
        if (this.plan != null) {
            this.plan.execute(Arrays.asList(PRE_SHUTDOWN, SHUTDOWN, POST_SHUTDOWN));
        }
        // compatibility
        this.onPlatformDisable();
    }

    /**
     * @deprecated Use method annotated with `@Planned(ExecutionPhase.STARTUP)`.
     */
    @Deprecated
    public void onPlatformEnable() {
    }

    /**
     * @deprecated Use method annotated with `@Planned(ExecutionPhase.SHUTDOWN)`.
     */
    @Deprecated
    public void onPlatformDisable() {
    }
}
