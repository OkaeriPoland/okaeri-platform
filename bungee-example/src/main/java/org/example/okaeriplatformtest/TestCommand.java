package org.example.okaeriplatformtest;

import eu.okaeri.commands.annotation.*;
import eu.okaeri.commands.bungee.annotation.Async;
import eu.okaeri.commands.bungee.annotation.Sync;
import eu.okaeri.commands.bungee.response.BungeeResponse;
import eu.okaeri.commands.service.CommandService;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.i18n.message.Message;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.placeholders.message.CompiledMessage;
import eu.okaeri.platform.bungee.i18n.BI18n;
import eu.okaeri.platform.core.annotation.Bean;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.example.okaeriplatformtest.config.TestConfig;
import org.example.okaeriplatformtest.config.TestLocaleConfig;
import org.example.okaeriplatformtest.persistence.PlayerProperties;
import org.example.okaeriplatformtest.persistence.PlayerRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Async
@Command(label = "testcmd", aliases = "testing")
public class TestCommand implements CommandService {

    private @Inject("testString") String test;
    private @Inject("exampleComplexBean") String complexContent;
    private @Inject TestConfig config;
    private @Inject BI18n i18n;
    private @Inject TestLocaleConfig messages;
    private @Inject Logger logger;
    private @Inject PlayerRepository playerPersistence;

    // testcmd|testing example
    @Sync
    @Executor
    public BungeeResponse example(@Label String label) {
        return BungeeResponse.ok("It works! {label} [{test}]")
            .with("label", label)
            .with("test", this.test);
    }

    // testcmd|testing complex
    @Executor(description = "wow async execution, db calls go brrr")
    public BungeeResponse complex() {
        return BungeeResponse.raw(this.complexContent, Thread.currentThread().getName());
    }

    // testcmd|testing greet|greeting
    @Executor(pattern = {"greet", "greeting"}, description = "greets you :O")
    public BungeeResponse greet(ExamplePlugin diExample, @Inject("testString") String namedDiExample) {
        return BungeeResponse.raw(this.config.getGreeting(), diExample.getDescription().getName(), namedDiExample);
    }

    // testcmd|testing i18n
    @Executor
    public Message i18n(CommandSender sender) {
        return this.i18n.get(sender, this.messages.getPlayerMessage())
            .with("sender", sender);
    }

    // testcmd|testing reload
    @Executor
    public Message reload(CommandSender sender) {

        try {
            this.config.load(); // reload config
            this.i18n.load(); // reload current i18n locales configs (no removing, no adding at runtime)
        } catch (Exception exception) {
            this.logger.log(Level.SEVERE, "Failed to reload configuration", exception);
            return this.i18n.get(sender, this.messages.getCommandsReloadFail());
        }

        return this.i18n.get(sender, this.messages.getCommandsReloadSuccess());
    }

    // testcmd|testing i18nbench
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
        sender.sendMessage(took + " " + (took / 1000));
        return message;
    }

    // testcmd|testing i18n <value>
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

    // testcmd|testing write1000players
    @Executor(pattern = "writeplayers *")
    public void writeplayers(CommandSender sender, @Arg("num") long num) {
        long start = System.currentTimeMillis();
        for (int i = 0; i < num; i++) {
            UUID randomPlayerId = UUID.randomUUID();
            PlayerProperties properties = this.playerPersistence.findOrCreateByPath(randomPlayerId);
            properties.setName(ThreadLocalRandom.current().ints('a', 'z' + 1)
                .limit(8)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString());
            properties.setLastJoined(Instant.ofEpochMilli(ThreadLocalRandom.current().nextLong(start)));
            properties.save();
        }
        long took = System.currentTimeMillis() - start;
        sender.sendMessage(took + " ms");
    }

    // testcmd|testing visittest
    @Executor
    public String visittest() {
        return this.playerPersistence.streamAll()
            .filter(entity -> entity.getUniqueId().getMostSignificantBits() > 250)
            .findFirst()
            .map(OkaeriConfig::saveToString)
            .orElse("no match found");
    }

    // testcmd|testing findplayer <name>
    @Executor(pattern = "findplayer *")
    public String findplayer(@Arg("name") String name) {
        long start = System.currentTimeMillis();
        String data = this.playerPersistence.findByName(name)
            .map(OkaeriConfig::saveToString)
            .orElse("huh");
        long took = System.currentTimeMillis() - start;
        data += "\n" + took + " ms";
        return data;
    }

    // testcmd|testing findbyworld <world>
    @Executor(pattern = "findbyworld *")
    public String findbyworld(@Arg("world") String worldName) {
        long start = System.currentTimeMillis();
        String data = this.playerPersistence.findByLastJoinedLocationWorld(worldName)
            .limit(10)
            .map(OkaeriConfig::saveToString)
            .collect(Collectors.joining("\n"));
        long took = System.currentTimeMillis() - start;
        data += "\n" + took + " ms";
        return data;
    }

    // testcmd|testing findbyworld <world>
    @Executor(pattern = "findbyy *")
    public String findbyy(@Arg("world") int y) {
        long start = System.currentTimeMillis();
        String data = this.playerPersistence.findByLastJoinedLocationY(y)
            .limit(10)
            .map(OkaeriConfig::saveToString)
            .collect(Collectors.joining("\n"));
        long took = System.currentTimeMillis() - start;
        data += "\n" + took + " ms";
        return data;
    }

    // testcmd|testing currentthread
    @Executor
    public String currentthread() {
        return Thread.currentThread().getName();
    }

    // testcmd|testing readallplayers
    @Executor
    public void readallplayers(CommandSender sender) {
        long start = System.currentTimeMillis();
        Collection<PlayerProperties> all = this.playerPersistence.findAll();
        sender.sendMessage(all.stream()
            .map(properties -> properties.getName() + ": " + properties.getLastJoined())
            .limit(100)
            .collect(Collectors.joining("\n")));
        long took = System.currentTimeMillis() - start;
        sender.sendMessage(took + " ms (records total: " + all.size() + ")");
    }

    // testcmd|testing deleteallplayers
    @Executor
    public void deleteallplayers(CommandSender sender) {
        long start = System.currentTimeMillis();
        sender.sendMessage("state: " + this.playerPersistence.deleteAll());
        long took = System.currentTimeMillis() - start;
        sender.sendMessage(took + " ms");
    }

    // testcmd|testing deleteme
    @Executor
    public void deleteme(@Context ProxiedPlayer player) {
        long start = System.currentTimeMillis();
        player.sendMessage("state: " + this.playerPersistence.deleteByPath(player.getUniqueId()));
        long took = System.currentTimeMillis() - start;
        player.sendMessage(took + " ms");
    }

    // testcmd|testing eval <message> - looks dangerous tbh
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

    @Bean("subbean")
    public String configureExampleSubbean() {
        return "BEAN FROM " + this.getClass() + "!!";
    }
}
