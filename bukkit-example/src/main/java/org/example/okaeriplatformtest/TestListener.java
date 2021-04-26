package org.example.okaeriplatformtest;

import eu.okaeri.commands.bukkit.handler.CommandsUnknownErrorEvent;
import eu.okaeri.commands.service.CommandContext;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.platform.bukkit.commons.teleport.QueuedTeleports;
import eu.okaeri.platform.core.annotation.Component;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;
import org.example.okaeriplatformtest.persistence.PlayerPersistence;
import org.example.okaeriplatformtest.persistence.PlayerProperties;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.logging.Logger;

@Component
public class TestListener implements Listener {

    @Inject private ExamplePlugin plugin;
    @Inject private Logger logger; // plugin's logger (name=logger)
    @Inject private Server server;
    @Inject private BukkitScheduler scheduler;

    @Inject private QueuedTeleports queuedTeleports;
    @Inject private PlayerPersistence playerPersistence;

    @Inject("subbean") private String subbeanString;
    @Inject("joinReward") ItemStack rewardItem;

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        // subbeans example
        event.setJoinMessage("Willkommen " + event.getPlayer().getName() + "! " + this.plugin.getName() + " is working!\n" + this.subbeanString);
        event.getPlayer().getInventory().addItem(this.rewardItem.clone());

        // accessing persistence layer should be always done async
        // this is especially true for loading and saving
        // failing to do this may cause lag spikes which can
        // range from minor for flat storages and major for databases
        this.scheduler.runTaskAsynchronously(this.plugin, () -> {

            // read example
            PlayerProperties playerProperties = this.playerPersistence.get(event.getPlayer());
            Instant lastJoined = playerProperties.getLastJoined(); // get current value
            event.getPlayer().sendMessage("Your last join time: " + lastJoined);
            playerProperties.setLastJoined(Instant.now()); // update value
            playerProperties.setLastJoinedLocation(event.getPlayer().getLocation()); // bukkit types thanks to SerdesBukkit work too

            // save player properties
            // normally this may not be required if data is not required to be saved immediately, see PlayerPersistence notes
            playerProperties.save();
        });
    }

    @EventHandler
    public void onAsyncChat(AsyncPlayerChatEvent event) {

        if (event.getMessage().contains("admin pls tp spawn")) {
            // notice how #teleport call is still allowed async
            // as it only creates the task and puts it in the queue
            Location spawnLocation = this.server.getWorlds().get(0).getSpawnLocation();
            this.queuedTeleports.teleport(event.getPlayer(), spawnLocation);
            return;
        }

        if (event.getMessage().contains("can i have fly")) {
            // you can use callback paramter if you want to make sure
            // actions are being executed only after teleportation happened
            Location locationTenUp = event.getPlayer().getLocation().add(0, 10, 0);
            this.queuedTeleports.teleport(event.getPlayer(), locationTenUp).thenAccept((player) -> player.sendMessage("Enjoy flying!"));
        }

        // logger demonstration
        this.logger.warning("WOW SOMEONE IS WRITING: " + event.getMessage());
    }

    @EventHandler
    public void onCommandsUnknownError(CommandsUnknownErrorEvent event) {

        // disable sending "Unknown error! Reference ID: {id}" message
        // event.setSendMessage(false);

        // fetch sender
        CommandContext commandContext = event.getCommandContext();
        CommandSender sender = commandContext.get("sender", CommandSender.class);
        if (sender == null) {
            return;
        }

        // useful properties
        // String errorId = event.getErrorId();
        // InvocationContext invocationContext = event.getInvocationContext();

        // custom handling, e.g. sentry
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        event.getCause().printStackTrace(pw);
        sender.sendMessage(sw.toString());
    }
}
