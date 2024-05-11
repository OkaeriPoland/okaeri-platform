package org.example.okaeriplatformtest;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.server.ServerPing;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.platform.core.annotation.Service;
import eu.okaeri.platform.velocity.component.type.listener.Listener;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;

@Service
public class ExampleListener implements Listener {

    private @Inject Logger logger;
    private @Inject ExampleConfig config;

    @Subscribe
    public void handlePing(ProxyPingEvent event) {
        this.logger.info("" + event);

        ServerPing ping = event.getPing().asBuilder()
            .description(Component.text(this.config.getMotd()))
            .build();

        event.setPing(ping);
    }
}
