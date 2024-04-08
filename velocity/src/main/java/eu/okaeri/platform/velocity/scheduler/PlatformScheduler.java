package eu.okaeri.platform.velocity.scheduler;

import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import com.velocitypowered.api.scheduler.Scheduler;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class PlatformScheduler {

    private final PluginContainer plugin;
    private final Scheduler scheduler;

    public void cancel(ScheduledTask task) {
        task.cancel();
    }

    public long cancelAll() {
        return this.scheduler
            .tasksByPlugin(this.plugin)
            .stream()
            .peek(ScheduledTask::cancel)
            .count();
    }

    public ScheduledTask runAsync(Runnable runnable) {
        return this.scheduler
            .buildTask(this.plugin, runnable)
            .schedule();
    }

    public ScheduledTask schedule(Runnable runnable, long rate, TimeUnit timeUnit) {
        return this.scheduler
            .buildTask(this.plugin, runnable)
            .repeat(rate, timeUnit)
            .schedule();
    }

    public ScheduledTask schedule(Runnable runnable, long delay, long rate, TimeUnit timeUnit) {
        return this.scheduler
            .buildTask(this.plugin, runnable)
            .delay(delay, timeUnit)
            .repeat(rate, timeUnit)
            .schedule();
    }
}
