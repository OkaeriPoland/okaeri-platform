package eu.okaeri.platform.bukkit.placeholderapi;

import eu.okaeri.placeholders.Placeholders;
import eu.okaeri.placeholders.bukkit.placeholderapi.PlaceholderApiPlaceholders;
import eu.okaeri.platform.bukkit.OkaeriBukkitPlugin;
import eu.okaeri.platform.core.plan.ExecutionTask;

import java.util.logging.Level;

public class BukkitPlaceholderApiTask implements ExecutionTask<OkaeriBukkitPlugin> {

    @Override
    public void execute(OkaeriBukkitPlugin platform) {

        if (!platform.getClass().isAnnotationPresent(EnablePlaceholderAPI.class)) {
            return;
        }

        if (platform.getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
            return;
        }

        platform.getInjector().getExact("placeholders", Placeholders.class).ifPresent(placeholders -> {
            try {
                placeholders.registerPlaceholders(new PlaceholderApiPlaceholders());
            } catch (Throwable throwable) {
                platform.getLogger().log(Level.WARNING, "Failed to register PlaceholderAPI placeholders (PlaceholderAPI via okaeri-placeholders)", throwable);
            }
            try {
                PlaceholderApiPlaceholders.registerBridge(platform, placeholders);
            } catch (Throwable throwable) {
                platform.getLogger().log(Level.WARNING, "Failed to register PlaceholderAPI bridge (okaeri-placeholders via PlaceholderAPI)", throwable);
            }
        });
    }
}
