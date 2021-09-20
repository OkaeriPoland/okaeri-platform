package org.example.okaeriplatformtest;

import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.persistence.cache.Cached;
import eu.okaeri.platform.bungee.annotation.Scheduled;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.concurrent.TimeUnit;

// example of scheduled component class
// async=true - simulating blocking fetching scenario
@Scheduled(rate = 5, timeUnit = TimeUnit.MINUTES)
public class TestTask implements Runnable {

    @Inject private ProxyServer proxy;
    @Inject("cachedDbData") private Cached<String> cachedData;

    @Override
    public void run() {

        // accessing Cached<T>
        String cachedValue = this.cachedData.get();
        this.proxy.broadcast(TextComponent.fromLegacyText(cachedValue));

        // accessing Cached<T> with forced update
        String updatedValue = this.cachedData.update();
        this.proxy.broadcast(TextComponent.fromLegacyText(updatedValue));
    }
}