//package eu.okaeri.platform.velocity.plan;
//
//import eu.okaeri.injector.Injector;
//import eu.okaeri.platform.velocity.OkaeriVelocityPlugin;
//import eu.okaeri.platform.core.component.ExternalResourceProvider;
//import eu.okaeri.platform.core.exception.BreakException;
//import eu.okaeri.platform.core.plan.ExecutionTask;
//import net.md_5.bungee.api.ProxyServer;
//import net.md_5.bungee.api.plugin.Plugin;
//
//import java.util.Optional;
//
//public class VelocityExternalResourceProviderSetupTask implements ExecutionTask<OkaeriVelocityPlugin> {
//
//    private static final ExternalResourceProvider EXTERNAL_RESOURCE_PROVIDER = (name, type, source) -> {
//
//        Plugin plugin = ProxyServer.getInstance().getPluginManager().getPlugins().stream()
//            .filter(proxyPlugin -> proxyPlugin.getClass() == source)
//            .findAny()
//            .orElse(null);
//
//        if (plugin == null) {
//            throw new BreakException("Cannot provide external resource: " + name + ", " + type + " from " + source + ": cannot find source");
//        }
//
//        Injector externalInjector = ((OkaeriVelocityPlugin) plugin).getInjector();
//        Optional<?> injectable = externalInjector.get(name, type);
//
//        if (!injectable.isPresent()) {
//            throw new BreakException("Cannot provide external resource: " + name + ", " + type + " from " + source + ": cannot find injectable");
//        }
//
//        return injectable.get();
//    };
//
//    @Override
//    public void execute(OkaeriVelocityPlugin platform) {
//
//        if (platform.getInjector().get("externalResourceProvider", ExternalResourceProvider.class).isPresent()) {
//            return;
//        }
//
//        platform.registerInjectable("externalResourceProvider", EXTERNAL_RESOURCE_PROVIDER);
//    }
//}
