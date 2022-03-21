package eu.okaeri.platform.bukkit.scheduler;

import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

@RequiredArgsConstructor
public class PlatformScheduler {

    private final Plugin plugin;
    private final BukkitScheduler scheduler;

    public void cancel(int taskId) {
        this.scheduler.cancelTask(taskId);
    }

    public void cancel(BukkitTask task) {
        task.cancel();
    }

    public void cancelAll() {
        this.scheduler.cancelTasks(this.plugin);
    }

    public BukkitTask runAsync(Runnable runnable) {
        return this.run(runnable, true);
    }

    public BukkitTask runSync(Runnable runnable) {
        return this.run(runnable, false);
    }

    public BukkitTask run(Runnable runnable, boolean async) {
        return async
            ? this.scheduler.runTaskAsynchronously(this.plugin, runnable)
            : this.scheduler.runTask(this.plugin, runnable);
    }

    public BukkitTask runLaterAsync(Runnable runnable, long delay) {
        return this.runLater(runnable, delay, true);
    }

    public BukkitTask runLaterSync(Runnable runnable, long delay) {
        return this.runLater(runnable, delay, false);
    }

    public BukkitTask runLater(Runnable runnable, long delay, boolean async) {
        return async
            ? this.scheduler.runTaskLaterAsynchronously(this.plugin, runnable, delay)
            : this.scheduler.runTaskLater(this.plugin, runnable, delay);
    }

    public BukkitTask runTimerAsync(Runnable runnable, long delay, long rate) {
        return this.runTimer(runnable, delay, rate, true);
    }

    public BukkitTask runTimerSync(Runnable runnable, long delay, long rate) {
        return this.runTimer(runnable, delay, rate, false);
    }

    public BukkitTask runTimer(Runnable runnable, long delay, long rate, boolean async) {
        return async
            ? this.scheduler.runTaskTimerAsynchronously(this.plugin, runnable, delay, rate)
            : this.scheduler.runTaskTimer(this.plugin, runnable, delay, rate);
    }
}
