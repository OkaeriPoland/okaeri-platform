package eu.okaeri.platform.bukkit.commons.teleport;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class TeleportAction {

    private final Player player;
    private final Location target;
    private final TeleportActionCallback callback;

    public TeleportAction(Player player, Location target) {
        this.player = player;
        this.target = target;
        this.callback = null;
    }

    public TeleportAction(Player player, Location target, TeleportActionCallback callback) {
        this.player = player;
        this.target = target;
        this.callback = callback;
    }

    @SuppressWarnings("unchecked")
    public void perform() {

        if (!this.player.isOnline()) {
            return;
        }

        Consumer<Boolean> consumer = success -> {

            if (!success) {
                Bukkit.getLogger().severe("Failed to teleport the player " + this.player + " to " + this.target);
                return;
            }

            if (this.callback == null) {
                return;
            }

            this.callback.teleported(this.player);
        };

        if (teleportAsync == null) {
            this.player.teleport(this.target);
            consumer.accept(true);
            return;
        }

        try {
            ((CompletableFuture<Boolean>) teleportAsync.invoke(this.player, this.target)).thenAccept(consumer);
        } catch (Throwable throwable) {
            consumer.accept(false);
        }
    }

    private static MethodHandle teleportAsync;

    static {
        try {
            Class<?> paperLib = Class.forName("io.papermc.lib.PaperLib");
            MethodHandles.Lookup lookup = MethodHandles.publicLookup();
            MethodType handleType = MethodType.methodType(Entity.class, Location.class);
            teleportAsync = lookup.findStatic(paperLib, "teleportAsync", handleType);
        }
        catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException ignored) {
        }
    }
}