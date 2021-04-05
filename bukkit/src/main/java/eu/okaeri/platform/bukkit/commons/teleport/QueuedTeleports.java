package eu.okaeri.platform.bukkit.commons.teleport;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

@Getter
public final class QueuedTeleports {

    private final Queue<TeleportAction> teleportQueue = new ConcurrentLinkedQueue<>();

    public CompletableFuture<Entity> teleport(Entity who, Location where) {
        CompletableFuture<Entity> future = new CompletableFuture<>();
        this.teleport(who, where, future::complete);
        return future;
    }

    public void teleport(Collection<? extends Entity> who, Location where, TeleportActionCallback callback) {
        new ArrayList<>(who).forEach(target -> this.teleport(target, where, callback));
    }

    public void teleport(Entity who, Location where, TeleportActionCallback callback) {
        if (who == null) throw new IllegalArgumentException("who cannot be null");
        if (where == null) throw new IllegalArgumentException("where cannot be null");
        this.teleportQueue.add(new TeleportAction(who, where, callback));
    }
}
