package eu.okaeri.platform.bungee;

import eu.okaeri.injector.Injector;
import eu.okaeri.platform.bungee.component.BungeeComponentCreator;
import eu.okaeri.platform.bungee.component.BungeeCreatorRegistry;
import eu.okaeri.platform.bungee.plan.BungeeExternalResourceProviderSetupTask;
import eu.okaeri.platform.bungee.plan.BungeeInjectablesSetupTask;
import eu.okaeri.platform.bungee.plan.BungeePlaceholdersSetupTask;
import eu.okaeri.platform.bungee.plan.BungeeSchedulerShutdownTask;
import eu.okaeri.platform.core.OkaeriPlatform;
import eu.okaeri.platform.core.component.creator.ComponentCreator;
import eu.okaeri.platform.core.plan.ExecutionPlan;
import eu.okaeri.platform.core.plan.ExecutionResult;
import eu.okaeri.platform.core.plan.task.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.Arrays;

import static eu.okaeri.platform.core.plan.ExecutionPhase.*;


public class OkaeriBungeePlugin extends Plugin implements OkaeriPlatform {

    @Getter @Setter private Injector injector;
    @Getter @Setter private ComponentCreator creator;

    @Override
    public void log(@NonNull String message) {
        this.getLogger().info(message);
    }

    @Override
    public void plan(@NonNull ExecutionPlan plan) {

        plan.add(PRE_SETUP, new InjectorSetupTask());
        plan.add(PRE_SETUP, new BungeePlaceholdersSetupTask());
        plan.add(PRE_SETUP, new BungeeInjectablesSetupTask());
        plan.add(PRE_SETUP, new CreatorSetupTask(BungeeComponentCreator.class, BungeeCreatorRegistry.class), "creator");

        plan.add(POST_SETUP, new BungeeExternalResourceProviderSetupTask());
        plan.add(POST_SETUP, new BeanManifestCreateTask());
        plan.add(POST_SETUP, new BeanManifestExecuteTask());

        plan.add(SHUTDOWN, new BungeeSchedulerShutdownTask());
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
