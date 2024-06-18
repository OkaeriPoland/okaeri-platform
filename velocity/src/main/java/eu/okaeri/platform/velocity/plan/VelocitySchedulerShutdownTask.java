package eu.okaeri.platform.velocity.plan;

import eu.okaeri.platform.core.plan.ExecutionTask;
import eu.okaeri.platform.velocity.OkaeriVelocityPlugin;
import eu.okaeri.platform.velocity.scheduler.PlatformScheduler;

public class VelocitySchedulerShutdownTask implements ExecutionTask<OkaeriVelocityPlugin> {

    @Override
    public void execute(OkaeriVelocityPlugin platform) {
        platform.getInjector().getExact("scheduler", PlatformScheduler.class)
            .ifPresent(PlatformScheduler::cancelAll);
    }
}
