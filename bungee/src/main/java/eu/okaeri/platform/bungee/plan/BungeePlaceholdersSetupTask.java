package eu.okaeri.platform.bungee.plan;

import eu.okaeri.placeholders.bungee.BungeePlaceholders;
import eu.okaeri.platform.bungee.OkaeriBungeePlugin;
import eu.okaeri.platform.core.plan.ExecutionTask;

public class BungeePlaceholdersSetupTask implements ExecutionTask<OkaeriBungeePlugin> {

    @Override
    public void execute(OkaeriBungeePlugin platform) {
        platform.registerInjectable("placeholders", BungeePlaceholders.create(true));
    }
}
