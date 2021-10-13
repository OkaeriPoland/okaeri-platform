package org.example.okaeriplatformtest.command;

import eu.okaeri.commands.annotation.Arg;
import eu.okaeri.commands.annotation.Executor;
import eu.okaeri.commands.annotation.RawArgs;
import eu.okaeri.commands.annotation.Command;
import eu.okaeri.commands.bukkit.response.BukkitResponse;
import eu.okaeri.commands.bukkit.response.RawResponse;
import eu.okaeri.commands.service.CommandService;
import eu.okaeri.i18n.message.Message;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.placeholders.message.CompiledMessage;
import eu.okaeri.platform.bukkit.i18n.BI18n;
import org.bukkit.command.CommandSender;
import org.example.okaeriplatformtest.ExamplePlugin;
import org.example.okaeriplatformtest.config.TestConfig;
import org.example.okaeriplatformtest.config.TestLocaleConfig;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

// this command can be preloaded thanks to limited dependencies
// see async mark (~) visible in the startup messages
@Command(label = "testpcmd", aliases = "testing")
public class TestPreloadedCommand implements CommandService {

    @Inject private TestConfig config;
    @Inject private BI18n i18n;
    @Inject private TestLocaleConfig messages;
    @Inject private Logger logger;

    // testpcmd|testing greet|greeting
    @Executor(pattern = {"greet", "greeting"}, description = "greets you :O")
    public BukkitResponse greet(ExamplePlugin diExample, @Inject("testString") String namedDiExample) {
        return RawResponse.of(this.config.getGreeting(), diExample.getName(), namedDiExample);
    }

    // testpcmd|testing i18n
    @Executor
    public Message i18n(CommandSender sender) {
        return this.i18n.get(sender, this.messages.getPlayerMessage())
                .with("sender", sender);
    }

    // testpcmd|testing reload
    @Executor
    public Message reload(CommandSender sender) {

        try {
            this.config.load(); // reload config
            this.i18n.load(); // reload current i18n locales configs (no removing, no adding at runtime)
        }
        catch (Exception exception) {
            this.logger.log(Level.SEVERE, "Failed to reload configuration", exception);
            return this.i18n.get(sender, this.messages.getCommandsReloadFail());
        }

        return this.i18n.get(sender, this.messages.getCommandsReloadSuccess());
    }

    // testpcmd|testing i18nbench
    @Executor
    public Message i18nbench(CommandSender sender) {
        Message message = this.i18n.get(sender, this.messages.getPlayerMessage()).with("sender", sender);
        long start = System.nanoTime();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            builder.append(message.apply());
        }
        long took = System.nanoTime() - start;
        sender.sendMessage(builder.substring(0, 1));
        sender.sendMessage(took + " " + (took / 1000) );
        return message;
    }

    // testpcmd|testing i18n <value>
    @Executor(pattern = "i18n *")
    public Message i18n(CommandSender sender, @Arg("value") String value) {
        this.logger.info("test i18n " + value);
        if ("1".equals(value)) {
            return this.i18n.get(sender, this.messages.getExampleMessage())
                    .with("who", 1);
        } else if ("2".equals(value)) {
            return this.i18n.get(sender, this.messages.getExampleMessage())
                    .with("who", 2);
        } else if ("you".equals(value)) {
            Message with = this.i18n.get(sender, this.messages.getExampleMessage())
                    .with("who", "you");
            this.logger.info(with.apply());
            return with;
        } else {
            return this.i18n.get(sender, this.messages.getExampleMessage());
        }
    }

    // testpcmd|testing currentthread
    @Executor(async = true)
    public String currentthread() {
        return Thread.currentThread().getName();
    }

    // testpcmd|testing eval <message> - looks dangerous tbh
    @Executor(pattern = "eval")
    public String eval(CommandSender sender, @RawArgs List<String> rawArgs) {

        String raw = String.join(" ", rawArgs.subList(1, rawArgs.size()));
        CompiledMessage message = CompiledMessage.of(raw);

        return this.i18n.getPlaceholders().contextOf(message)
                .with("sender", sender)
                .apply();
    }

    @Executor
    public String exception() {
        throw new RuntimeException("rrrrrr");
    }
}
