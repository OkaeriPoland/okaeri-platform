package org.example.okaeriplatformtest;

import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.platform.core.annotation.Component;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.scheduler.TaskScheduler;
import net.md_5.bungee.event.EventHandler;
import org.example.okaeriplatformtest.config.TestConfig;
import org.example.okaeriplatformtest.persistence.PlayerProperties;
import org.example.okaeriplatformtest.persistence.PlayerRepository;

import java.lang.reflect.Proxy;
import java.time.Instant;
import java.util.logging.Logger;

@Component
public class TestListener implements Listener {

    @Inject private ExamplePlugin plugin;
    @Inject private Logger logger; // plugin's logger (name=logger)
    @Inject private Proxy server;
    @Inject private TaskScheduler scheduler;
    @Inject private TestConfig config;

    @Inject private PlayerRepository playerPersistence;

//    @Inject("subbean") private String subbeanString;

    @EventHandler
    public void onJoin(ServerConnectEvent event) {

        // subbeans example
        BaseComponent[] welcomeText = TextComponent.fromLegacyText("Willkommen " + event.getPlayer().getName() + "! " + this.plugin.getDescription().getName() + " is working!\n" + null /*subbeanString */);
        event.getPlayer().sendMessage(welcomeText);

        // accessing persistence layer should be always done async
        // this is especially true for loading and saving
        // failing to do this may cause lag spikes which can
        // range from minor for flat storages and major for databases
        this.scheduler.runAsync(this.plugin, () -> {

            // read example
            PlayerProperties playerProperties = this.playerPersistence.get(event.getPlayer());
            Instant lastJoined = playerProperties.getLastJoined(); // get current value
            event.getPlayer().sendMessage(TextComponent.fromLegacyText("Your last join time: " + lastJoined));
            playerProperties.setLastJoined(Instant.now()); // update value

            // save player properties
            // normally this may not be required if data is not required to be saved immediately, see PlayerPersistence notes
            playerProperties.save();
        });
    }

//    @EventHandler TODO: commands
//    public void onCommandsUnknownError(CommandsUnknownErrorEvent event) {
//
//        // disable sending "Unknown error! Reference ID: {id}" message
//        // event.setSendMessage(false);
//
//        // fetch sender
//        CommandContext commandContext = event.getCommandContext();
//        CommandSender sender = commandContext.get("sender", CommandSender.class);
//        if (sender == null) {
//            return;
//        }
//
//        // useful properties
//        // String errorId = event.getErrorId();
//        // InvocationContext invocationContext = event.getInvocationContext();
//
//        // custom handling, e.g. sentry
//        StringWriter sw = new StringWriter();
//        PrintWriter pw = new PrintWriter(sw);
//        event.getCause().printStackTrace(pw);
//        sender.sendMessage(sw.toString());
//    }
}
