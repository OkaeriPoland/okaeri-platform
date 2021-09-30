package eu.okaeri.platform.bukkit.plan;

import eu.okaeri.placeholders.bukkit.BukkitPlaceholders;
import eu.okaeri.platform.bukkit.OkaeriBukkitPlugin;
import eu.okaeri.platform.core.plan.ExecutionTask;

public class BukkitPlaceholdersSetupTask implements ExecutionTask<OkaeriBukkitPlugin> {

    @Override
    public void execute(OkaeriBukkitPlugin platform) {
        platform.registerInjectable("placeholders", BukkitPlaceholders.create(true));
    }
}
