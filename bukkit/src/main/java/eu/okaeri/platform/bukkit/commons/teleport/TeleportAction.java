package eu.okaeri.platform.bukkit.commons.teleport;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class TeleportAction {

    private final Entity who;
    private final Location where;
    private final TeleportActionCallback callback;

    public TeleportAction(Entity who, Location where, TeleportActionCallback callback) {
        this.who = who;
        this.where = where;
        this.callback = callback;
    }

    @SuppressWarnings("unchecked")
    public void perform() {

        if ((this.who instanceof Player) && !((Player) this.who).isOnline()) {
            return;
        }

        Consumer<Boolean> consumer = success -> {

            if (!success) {
                Bukkit.getLogger().severe("Failed to teleport the player " + this.who + " to " + this.where);
                return;
            }

            if (this.callback == null) {
                return;
            }

            this.callback.teleported(this.who);
        };

        if (paperTeleportAsync != null) {
            try {
                ((CompletableFuture<Boolean>) paperTeleportAsync.invoke(this.who, this.where)).thenAccept(consumer);
                return;
            } catch (Throwable ignored) {
            }
        }

        if (entityTeleportAsync != null) {
            try {
                entityTeleportAsync.invoke(this.who, PlayerTeleportEvent.TeleportCause.PLUGIN);
                return;
            } catch (Throwable ignored) {
            }
        }

        this.who.teleport(this.where);
        consumer.accept(true);
    }

    private void teleport() {

    }

    private static MethodHandle paperTeleportAsync;
    private static MethodHandle entityTeleportAsync;

    static {
        try {
            Class<?> paperLib = Class.forName("io.papermc.lib.PaperLib");
            MethodHandles.Lookup lookup = MethodHandles.publicLookup();
            MethodType handleType = MethodType.methodType(Entity.class, Location.class);
            paperTeleportAsync = lookup.findStatic(paperLib, "teleportAsync", handleType);
        }
        catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException ignored) {
            // try fallback 1.13+ method
            try {
                MethodHandles.Lookup lookup = MethodHandles.publicLookup();
                Method teleportAsyncEntity = Entity.class.getMethod("teleportAsync", Location.class, PlayerTeleportEvent.TeleportCause.class);
                entityTeleportAsync = lookup.unreflect(teleportAsyncEntity);
            }
            catch (NoSuchMethodException | IllegalAccessException ignored1) {
            }
        }
    }
}