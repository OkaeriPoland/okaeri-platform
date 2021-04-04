package eu.okaeri.platform.bukkit.commons.teleport;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.Queue;

public class QueuedTeleportsTask implements Runnable {

    private final Queue<TeleportAction> queue;
    private final JavaPlugin plugin;
    private final int teleportsPerRun;

    public QueuedTeleportsTask(QueuedTeleports queuedTeleports, JavaPlugin plugin) {
        this(queuedTeleports, plugin, 1);
    }

    public QueuedTeleportsTask(QueuedTeleports queuedTeleports, JavaPlugin plugin, int teleportsPerRun) {
        if (queuedTeleports == null) throw new IllegalArgumentException("queuedTeleports cannot be null");
        if (plugin == null) throw new IllegalArgumentException("plugin cannot be null");
        this.queue = queuedTeleports.getTeleportQueue();
        this.plugin = plugin;
        this.teleportsPerRun = teleportsPerRun;
    }

    @Override
    public void run() {

        long start = System.nanoTime();
        int actionsPerformed = 0;

        for (int i = 0; i < this.teleportsPerRun; i++) {
            TeleportAction action = this.queue.poll();
            if (action == null) {
                continue;
            }
            action.perform();
            actionsPerformed++;
        }

        if (actionsPerformed == 0) {
            return;
        }

        long took = System.nanoTime() - start;
        this.plugin.getLogger().info("Teleport Queue [-" + actionsPerformed + "]: " + this.queue.size() + " actions left (" + took + " ns, " + took / 1000L / 1000L + " ms)");
    }
}
