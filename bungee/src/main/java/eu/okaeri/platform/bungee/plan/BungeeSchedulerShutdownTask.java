package eu.okaeri.platform.bungee.plan;

import eu.okaeri.injector.Injectable;
import eu.okaeri.platform.bungee.OkaeriBungeePlugin;
import eu.okaeri.platform.bungee.scheduler.PlatformScheduler;
import eu.okaeri.platform.core.plan.ExecutionTask;

public class BungeeSchedulerShutdownTask implements ExecutionTask<OkaeriBungeePlugin> {

    @Override
    public void execute(OkaeriBungeePlugin platform) {
        platform.getInjector().getExact("scheduler", PlatformScheduler.class)
                .map(Injectable::getObject)
                .ifPresent(PlatformScheduler::cancelAll);
    }
}
