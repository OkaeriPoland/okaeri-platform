package org.example.okaeriplatformtest;

import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.platform.bukkit.annotation.Timer;
import eu.okaeri.platform.bukkit.commons.command.CommandRunner;
import eu.okaeri.platform.bukkit.commons.time.MinecraftTimeEquivalent;
import eu.okaeri.platform.core.persistence.cache.Cached;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.HumanEntity;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;

// example of timer component class
// async=true - simulating blocking fetching scenario
@Timer(rate = MinecraftTimeEquivalent.MINUTES_5, async = true)
public class TestTask implements Runnable {

    @Inject private TestConfig config;
    @Inject private Server server;
    @Inject private Plugin plugin;

    @Inject("cachedDbData")
    private Cached<String> cachedData;

    @Override
    public void run() {

        // built-in CommandRunner for easy exectution
        // of commands e.g. from the configuration/web/other source
        CommandRunner.of(this.plugin, this.server.getOnlinePlayers()) // accepts any single element or collection
                .forceMainThread(true) // forces execution on the main thread
                .withField("{ending}", "hmmm..")
                .withField("{name}", HumanEntity::getName) // dynamic replaces based on current element or static values
                .execute(Arrays.asList("say how are you {name}? {ending}", this.config.getRepeatingCommand())); // pass single element or collection of commands

        // accessing Cached<T>
        String cachedValue = this.cachedData.get();
        Bukkit.broadcastMessage(cachedValue);

        // accessing Cached<T> with forced update
        String updatedValue = this.cachedData.update();
        Bukkit.broadcastMessage(updatedValue);
    }
}