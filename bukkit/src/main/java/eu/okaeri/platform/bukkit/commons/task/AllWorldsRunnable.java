package eu.okaeri.platform.bukkit.commons.task;

import org.bukkit.Bukkit;
import org.bukkit.World;

public abstract class AllWorldsRunnable implements Runnable {

    @Override
    public void run() {
        Bukkit.getWorlds().forEach(this::runFor);
    }

    public abstract void runFor(World world);
}
