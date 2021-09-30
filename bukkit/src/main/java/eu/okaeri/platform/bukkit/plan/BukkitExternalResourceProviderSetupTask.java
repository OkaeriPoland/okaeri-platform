package eu.okaeri.platform.bukkit.plan;

import eu.okaeri.injector.Injectable;
import eu.okaeri.injector.Injector;
import eu.okaeri.platform.bukkit.OkaeriBukkitPlugin;
import eu.okaeri.platform.core.component.ExternalResourceProvider;
import eu.okaeri.platform.core.exception.BreakException;
import eu.okaeri.platform.core.plan.ExecutionTask;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;

public class BukkitExternalResourceProviderSetupTask implements ExecutionTask<OkaeriBukkitPlugin> {

    @SuppressWarnings("unchecked") private static final ExternalResourceProvider EXTERNAL_RESOURCE_PROVIDER = (name, type, source) -> {

        Class<? extends JavaPlugin> sourcePlugin = (Class<? extends JavaPlugin>) source;
        JavaPlugin plugin = JavaPlugin.getPlugin(sourcePlugin);

        if (plugin == null) {
            throw new BreakException("Cannot provide external resource: " + name + ", " + type + " from " + source + ": cannot find source");
        }

        Injector externalInjector = ((OkaeriBukkitPlugin) plugin).getInjector();
        Optional<? extends Injectable<?>> injectable = externalInjector.getInjectable(name, type);

        if (!injectable.isPresent()) {
            throw new BreakException("Cannot provide external resource: " + name + ", " + type + " from " + source + ": cannot find injectable");
        }

        return injectable.get().getObject();
    };

    @Override
    public void execute(OkaeriBukkitPlugin platform) {

        if (platform.getInjector().getInjectable("externalResourceProvider", ExternalResourceProvider.class).isPresent()) {
            return;
        }

        platform.registerInjectable("externalResourceProvider", EXTERNAL_RESOURCE_PROVIDER);
    }
}
