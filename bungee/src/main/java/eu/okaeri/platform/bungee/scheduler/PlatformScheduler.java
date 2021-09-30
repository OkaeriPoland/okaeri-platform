package eu.okaeri.platform.bungee.scheduler;

import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.api.scheduler.TaskScheduler;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class PlatformScheduler {

    private final Plugin plugin;
    private final TaskScheduler scheduler;

    public void cancel(int taskId) {
        this.scheduler.cancel(taskId);
    }

    public void cancel(ScheduledTask task) {
        this.scheduler.cancel(task);
    }

    public int cancelAll() {
        return this.scheduler.cancel(this.plugin);
    }

    public ScheduledTask runAsync(Runnable runnable) {
        return this.scheduler.runAsync(this.plugin, runnable);
    }

    public ScheduledTask schedule(Runnable runnable, long rate, TimeUnit timeUnit) {
        return this.scheduler.schedule(this.plugin, runnable, rate, timeUnit);
    }

    public ScheduledTask schedule(Runnable runnable, long delay, long rate, TimeUnit timeUnit) {
        return this.scheduler.schedule(this.plugin, runnable, delay, rate, timeUnit);
    }
}
