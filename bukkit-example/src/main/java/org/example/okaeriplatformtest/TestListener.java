package org.example.okaeriplatformtest;

import eu.okaeri.injector.annotation.Inject;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class TestListener implements Listener {

    @Inject private ExamplePlugin plugin;
    @Inject("subbean") private String subbeanString;

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.setJoinMessage("Willkommen " + event.getPlayer().getName() + "! " + this.plugin.getName() + " is working!\n" + this.subbeanString);
    }
}
