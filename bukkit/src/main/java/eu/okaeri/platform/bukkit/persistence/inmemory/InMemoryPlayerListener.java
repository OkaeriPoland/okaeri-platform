package eu.okaeri.platform.bukkit.persistence.inmemory;

import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;

@RequiredArgsConstructor
class InMemoryPlayerListener<T extends TrackedDocument> implements Listener {

    private final InMemoryPlayerHandler<T> handler;

    @EventHandler(priority = EventPriority.MONITOR)
    public void handleLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            return;
        }
        this.handler.get(event.getUniqueId()); // side effect
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void handleQuit(PlayerQuitEvent event) {
        T removed = this.handler.cache.remove(event.getPlayer().getUniqueId());
        if (removed == null || !removed.clearDirty()) {
            return;
        }
        this.handler.plugin.getServer().getScheduler().runTaskAsynchronously(this.handler.plugin, removed::save);
    }

    @EventHandler
    public void handleDisable(PluginDisableEvent event) {
        if (event.getPlugin() != this.handler.plugin) {
            return;
        }
        this.handler.shutdown();
    }
}
