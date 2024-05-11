package org.example.okaeriplatformtest;

import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import eu.okaeri.platform.core.annotation.Scan;
import eu.okaeri.platform.core.plan.ExecutionPhase;
import eu.okaeri.platform.core.plan.Planned;
import eu.okaeri.platform.velocity.OkaeriVelocityPlugin;
import eu.okaeri.platform.velocity.annotation.Delayed;
import eu.okaeri.platform.velocity.annotation.Scheduled;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;


@Scan(exclusions = "org.example.okaeriplatformtest.libs", deep = true)
@Plugin(id = "okaeriplatformtest", name = "okaeri-platform test", version = "0.0.1", authors = {"Dawid Sawicki <dawid@okaeri.cloud>"})
public class ExamplePlugin extends OkaeriVelocityPlugin {

    @Planned(ExecutionPhase.STARTUP)
    public void onStartup(Logger logger) {
        logger.info("Enabled!");
    }

    @Planned(ExecutionPhase.SHUTDOWN)
    public void onShutdown(Logger logger) {
        logger.info("Disabled!");
    }

    @Scheduled(rate = 1, timeUnit = TimeUnit.MINUTES)
    public void exampleTimer(ProxyServer proxy) {
        TextComponent message = Component.text("Hi there??");
        proxy.getAllPlayers().forEach(player -> player.sendMessage(message));
    }

    @Delayed(time = 1, timeUnit = TimeUnit.SECONDS)
    public void runAfterServerIsFullyLoaded(Logger logger) {
        logger.info("Looks like server is now fully loaded!");
    }
}
