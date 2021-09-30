package org.example.okaeriplatformtest;

import eu.okaeri.commons.bukkit.command.CommandRunner;
import eu.okaeri.commons.bukkit.time.MinecraftTimeEquivalent;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.persistence.cache.Cached;
import eu.okaeri.platform.bukkit.annotation.Scheduled;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.HumanEntity;
import org.bukkit.plugin.Plugin;
import org.example.okaeriplatformtest.config.TestConfig;

import java.util.Arrays;

// example of scheduled component class
// async=true - simulating blocking fetching scenario
@Scheduled(rate = MinecraftTimeEquivalent.MINUTE * 5, async = true)
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
                .field("ending", "hmmm..")
                .field("name", HumanEntity::getName) // dynamic replaces based on current element or static values
                .execute(Arrays.asList("say how are you {name}? {ending}", this.config.getRepeatingCommand())); // pass single element or collection of commands

        // accessing Cached<T>
        String cachedValue = this.cachedData.get();
        Bukkit.broadcastMessage(cachedValue);

        // accessing Cached<T> with forced update
        String updatedValue = this.cachedData.update();
        Bukkit.broadcastMessage(updatedValue);
    }
}