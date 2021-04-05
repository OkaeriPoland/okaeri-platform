package org.example.okaeriplatformtest;

import eu.okaeri.commands.annotation.Executor;
import eu.okaeri.commands.annotation.Label;
import eu.okaeri.commands.annotation.ServiceDescriptor;
import eu.okaeri.commands.bukkit.response.BukkitResponse;
import eu.okaeri.commands.bukkit.response.ErrorResponse;
import eu.okaeri.commands.bukkit.response.RawResponse;
import eu.okaeri.commands.bukkit.response.SuccessResponse;
import eu.okaeri.commands.service.CommandService;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.platform.bukkit.commons.teleport.QueuedTeleports;
import eu.okaeri.platform.core.annotation.Bean;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@ServiceDescriptor(label = "testcmd", aliases = "testing")
public class TestCommand implements CommandService {

    @Inject("testString") private String test;
    @Inject("exampleComplexBean") private String complexContent;
    @Inject private TestConfig config;
    @Inject private Server server;
    @Inject private QueuedTeleports teleports;

    // testcmd|testing example
    @Executor
    public BukkitResponse example(@Label String label) {
        return SuccessResponse.of("It works! {label} [{test}]")
                .withField("{label}", label)
                .withField("{test}", this.test);
    }

    // testcmd|testing complex
    @Executor(async = true, description = "wow async execution, db calls go brrr")
    public BukkitResponse complex() {
        return RawResponse.of(this.complexContent, Thread.currentThread().getName());
    }

    // testcmd|testing greet|greeting
    @Executor(pattern = {"greet", "greeting"}, description = "greets you :O")
    public BukkitResponse greet(ExamplePlugin diExample, @Inject("testString") String namedDiExample) {
        return RawResponse.of(this.config.getGreeting(), diExample.getName(), namedDiExample);
    }

    // testcmd|testing tphereall
    @Executor(async = true)
    public BukkitResponse tphereall(CommandSender sender) {

        /* waiting for okaeri-commands to use @Sender Player directly - not funny */
        if (!(sender instanceof Player)) {
            return ErrorResponse.of("Sorry we cannot do that!");
        }

        Player player = (Player) sender;
        Location playerLocation = player.getLocation();

        // example of teleporting multiple players with QueuedTeleports
        // for simplicity player using the command is teleported too
        // notice how we use #join() on the future - this operation is blocking
        // and thus requires executor to be async or scheduler to be used
        // alternatively if awaiting for all teleports is not required one may
        // use teleport(Collection<? extends Entity>, Location, TeleportActionCallback) instead
        this.server.getOnlinePlayers().stream()
                .map(target -> this.teleports.teleport(target, playerLocation).join())
                .forEach((target) -> SuccessResponse.of("You have been teleported here by {player}!")
                        .withField("{player}", player)
                        .sendTo(target));

        // respond to the player after everyone is teleported
        return SuccessResponse.of("Please welcome your new friends!");
    }

    @Bean("subbean")
    public String configureExampleSubbean() {
        return "BEAN FROM " + this.getClass() + "!!";
    }
}
