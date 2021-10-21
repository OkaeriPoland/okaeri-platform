package eu.okaeri.platform.bungee.plan;

import eu.okaeri.injector.Injector;
import eu.okaeri.platform.bungee.OkaeriBungeePlugin;
import eu.okaeri.platform.core.component.ExternalResourceProvider;
import eu.okaeri.platform.core.exception.BreakException;
import eu.okaeri.platform.core.plan.ExecutionTask;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.Optional;

public class BungeeExternalResourceProviderSetupTask implements ExecutionTask<OkaeriBungeePlugin> {

    private static final ExternalResourceProvider EXTERNAL_RESOURCE_PROVIDER = (name, type, source) -> {

        Plugin plugin = ProxyServer.getInstance().getPluginManager().getPlugins().stream()
                .filter(proxyPlugin -> proxyPlugin.getClass() == source)
                .findAny()
                .orElse(null);

        if (plugin == null) {
            throw new BreakException("Cannot provide external resource: " + name + ", " + type + " from " + source + ": cannot find source");
        }

        Injector externalInjector = ((OkaeriBungeePlugin) plugin).getInjector();
        Optional<?> injectable = externalInjector.get(name, type);

        if (!injectable.isPresent()) {
            throw new BreakException("Cannot provide external resource: " + name + ", " + type + " from " + source + ": cannot find injectable");
        }

        return injectable.get();
    };

    @Override
    public void execute(OkaeriBungeePlugin platform) {

        if (platform.getInjector().get("externalResourceProvider", ExternalResourceProvider.class).isPresent()) {
            return;
        }

        platform.registerInjectable("externalResourceProvider", EXTERNAL_RESOURCE_PROVIDER);
    }
}
