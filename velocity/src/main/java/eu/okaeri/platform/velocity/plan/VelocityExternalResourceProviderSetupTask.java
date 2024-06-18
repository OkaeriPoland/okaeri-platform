package eu.okaeri.platform.velocity.plan;

import com.velocitypowered.api.plugin.PluginContainer;
import eu.okaeri.injector.Injector;
import eu.okaeri.platform.velocity.OkaeriVelocityPlugin;
import eu.okaeri.platform.core.component.ExternalResourceProvider;
import eu.okaeri.platform.core.exception.BreakException;
import eu.okaeri.platform.core.plan.ExecutionTask;

import java.util.Optional;

public class VelocityExternalResourceProviderSetupTask implements ExecutionTask<OkaeriVelocityPlugin> {

    @Override
    public void execute(OkaeriVelocityPlugin platform) {

        if (platform.getInjector().get("externalResourceProvider", ExternalResourceProvider.class).isPresent()) {
            return;
        }

        platform.registerInjectable("externalResourceProvider", (ExternalResourceProvider) (name, type, source) -> {

            Object plugin = platform.getProxy().getPluginManager().getPlugins().stream()
                .map(PluginContainer::getInstance)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(proxyPlugin -> proxyPlugin.getClass() == type)
                .findAny()
                .orElse(null);

            if (!(plugin instanceof OkaeriVelocityPlugin)) {
                throw new BreakException("Cannot provide external resource: " + name + ", " + type + " from " + source + ": cannot find source");
            }

            Injector externalInjector = ((OkaeriVelocityPlugin) plugin).getInjector();
            Optional<?> injectable = externalInjector.get(name, type);

            if (!injectable.isPresent()) {
                throw new BreakException("Cannot provide external resource: " + name + ", " + type + " from " + source + ": cannot find injectable");
            }

            return injectable.get();
        });
    }
}
