package eu.okaeri.platform.bukkit.commons.task;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public abstract class OnlinePlayersRunnable implements Runnable {

    @Override
    public void run() {
        Bukkit.getOnlinePlayers().forEach(this::runFor);
    }

    public abstract void runFor(Player player);
}
