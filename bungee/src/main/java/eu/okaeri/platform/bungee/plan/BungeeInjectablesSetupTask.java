package eu.okaeri.platform.bungee.plan;

import eu.okaeri.configs.serdes.commons.SerdesCommons;
import eu.okaeri.configs.yaml.bukkit.serdes.SerdesBungee;
import eu.okaeri.configs.yaml.bungee.YamlBungeeConfigurer;
import eu.okaeri.persistence.document.ConfigurerProvider;
import eu.okaeri.platform.bungee.OkaeriBungeePlugin;
import eu.okaeri.platform.bungee.i18n.ProxiedPlayerLocaleProvider;
import eu.okaeri.platform.bungee.scheduler.PlatformScheduler;
import eu.okaeri.platform.core.placeholder.SimplePlaceholdersFactory;
import eu.okaeri.platform.core.plan.ExecutionTask;

public class BungeeInjectablesSetupTask implements ExecutionTask<OkaeriBungeePlugin> {

    @Override
    public void execute(OkaeriBungeePlugin platform) {

        platform.registerInjectable("proxy", platform.getProxy());
        platform.registerInjectable("dataFolder", platform.getDataFolder());
        platform.registerInjectable("logger", platform.getLogger());
        platform.registerInjectable("plugin", platform);
        platform.registerInjectable("scheduler", new PlatformScheduler(platform, platform.getProxy().getScheduler()));
        platform.registerInjectable("pluginManager", platform.getProxy().getPluginManager());

        platform.registerInjectable("defaultConfigurerProvider", (ConfigurerProvider) YamlBungeeConfigurer::new);
        platform.registerInjectable("defaultConfigurerSerdes", new Class[]{SerdesBungee.class, SerdesCommons.class});
        platform.registerInjectable("defaultPlaceholdersFactory", new SimplePlaceholdersFactory());
        platform.registerInjectable("i18nLocaleProvider", new ProxiedPlayerLocaleProvider());
    }
}
