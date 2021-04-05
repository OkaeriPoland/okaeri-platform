package eu.okaeri.platform.bukkit.commons.teleport;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

@Getter
public final class QueuedTeleports {

    private final Queue<TeleportAction> teleportQueue = new ConcurrentLinkedQueue<>();

    public CompletableFuture<Player> teleport(Player player, Location target) {
        CompletableFuture<Player> future = new CompletableFuture<>();
        this.teleport(player, target, future::complete);
        return future;
    }

    public void teleport(Player player, Location target, TeleportActionCallback callback) {
        if (player == null) throw new IllegalArgumentException("player cannot be null");
        if (target == null) throw new IllegalArgumentException("target cannot be null");
        this.teleportQueue.add(new TeleportAction(player, target, callback));
    }
}
