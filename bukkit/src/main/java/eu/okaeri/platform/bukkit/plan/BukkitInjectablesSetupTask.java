package eu.okaeri.platform.bukkit.plan;

import eu.okaeri.configs.serdes.commons.SerdesCommons;
import eu.okaeri.configs.yaml.bukkit.YamlBukkitConfigurer;
import eu.okaeri.configs.yaml.bukkit.serdes.SerdesBukkit;
import eu.okaeri.persistence.document.ConfigurerProvider;
import eu.okaeri.platform.bukkit.OkaeriBukkitPlugin;
import eu.okaeri.platform.bukkit.i18n.PlayerLocaleProvider;
import eu.okaeri.platform.bukkit.scheduler.PlatformScheduler;
import eu.okaeri.platform.core.placeholder.SimplePlaceholdersFactory;
import eu.okaeri.platform.core.plan.ExecutionTask;
import eu.okaeri.tasker.bukkit.BukkitTasker;

public class BukkitInjectablesSetupTask implements ExecutionTask<OkaeriBukkitPlugin> {

    @Override
    public void execute(OkaeriBukkitPlugin platform) {

        platform.registerInjectable("server", platform.getServer());
        platform.registerInjectable("dataFolder", platform.getDataFolder());
        platform.registerInjectable("jarFile", platform.getFile());
        platform.registerInjectable("logger", platform.getLogger());
        platform.registerInjectable("plugin", platform);
        platform.registerInjectable("scheduler", new PlatformScheduler(platform, platform.getServer().getScheduler()));
        platform.registerInjectable("tasker", BukkitTasker.newPool(platform));
        platform.registerInjectable("pluginManager", platform.getServer().getPluginManager());

        platform.registerInjectable("defaultConfigurerProvider", (ConfigurerProvider) YamlBukkitConfigurer::new);
        platform.registerInjectable("defaultConfigurerSerdes", new Class[]{SerdesBukkit.class, SerdesCommons.class});
        platform.registerInjectable("defaultPlaceholdersFactory", new SimplePlaceholdersFactory());
        platform.registerInjectable("i18nLocaleProvider", new PlayerLocaleProvider());
    }
}
