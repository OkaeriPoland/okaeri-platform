package eu.okaeri.platform.bukkit.component;

import eu.okaeri.platform.bukkit.OkaeriBukkitPlugin;
import eu.okaeri.platform.core.annotation.Scan;
import eu.okaeri.platform.core.component.creator.DefaultScanRequirementHandler;
import lombok.NonNull;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

public class BukkitScanRequirementHandler extends DefaultScanRequirementHandler {

    protected static final String REQ_PLUGIN = "plugin:";

    public BukkitScanRequirementHandler(@NonNull OkaeriBukkitPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean meetsRequirement(@NonNull Class<?> parent, @NonNull Scan scan, @NonNull List<String> requires) {

        boolean meets = true;
        List<String> remaining = new ArrayList<>(requires);

        for (String requirement : requires) {
            if (requirement.startsWith(REQ_PLUGIN)) {
                String pluginName = requirement.substring(REQ_PLUGIN.length());
                if (!Bukkit.getPluginManager().isPluginEnabled(pluginName)) {
                    this.fail(parent, scan, requirement);
                    meets = false;
                }
                remaining.remove(requirement);
            }
        }

        return meets && super.meetsRequirement(parent, scan, remaining);
    }
}
