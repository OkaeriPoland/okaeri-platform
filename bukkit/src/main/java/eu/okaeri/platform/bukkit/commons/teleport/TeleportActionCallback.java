package eu.okaeri.platform.bukkit.commons.teleport;

import org.bukkit.entity.Entity;

public interface TeleportActionCallback {
    void teleported(Entity who);
}
