//package org.example.okaeriplatformtest;
//
//import eu.okaeri.commands.annotation.*;
//import eu.okaeri.commands.bukkit.annotation.Sender;
//import eu.okaeri.commands.bukkit.response.BukkitResponse;
//import eu.okaeri.commands.bukkit.response.ErrorResponse;
//import eu.okaeri.commands.bukkit.response.RawResponse;
//import eu.okaeri.commands.bukkit.response.SuccessResponse;
//import eu.okaeri.commands.service.CommandService;
//import eu.okaeri.commons.bukkit.teleport.QueuedTeleports;
//import eu.okaeri.configs.OkaeriConfig;
//import eu.okaeri.i18n.message.Message;
//import eu.okaeri.injector.annotation.Inject;
//import eu.okaeri.placeholders.message.CompiledMessage;
//import eu.okaeri.platform.bukkit.i18n.BI18n;
//import eu.okaeri.platform.core.annotation.Bean;
//import org.apache.commons.lang.RandomStringUtils;
//import org.bukkit.*;
//import org.bukkit.command.CommandSender;
//import org.bukkit.entity.Player;
//import org.example.okaeriplatformtest.config.TestConfig;
//import org.example.okaeriplatformtest.config.TestLocaleConfig;
//import org.example.okaeriplatformtest.persistence.PlayerProperties;
//import org.example.okaeriplatformtest.persistence.PlayerRepository;
//
//import java.time.Instant;
//import java.util.Collection;
//import java.util.List;
//import java.util.UUID;
//import java.util.concurrent.ThreadLocalRandom;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import java.util.stream.Collectors;
//
//@Command(label = "testcmd", aliases = "testing")
//public class TestCommand implements CommandService {
//
//    @Inject("testString") private String test;
//    @Inject("exampleComplexBean") private String complexContent;
//    @Inject private TestConfig config;
//    @Inject private Server server;
//    @Inject private QueuedTeleports teleports;
//    @Inject private BI18n i18n;
//    @Inject private TestLocaleConfig messages;
//    @Inject private Logger logger;
//    @Inject private PlayerRepository playerPersistence;
//
//    // testcmd|testing example
//    @Executor
//    public BukkitResponse example(@Label String label) {
//        return SuccessResponse.of("It works! {label} [{test}]")
//                .withField("label", label)
//                .withField("test", this.test);
//    }
//
//    // testcmd|testing complex
//    @Executor(async = true, description = "wow async execution, db calls go brrr")
//    public BukkitResponse complex() {
//        return RawResponse.of(this.complexContent, Thread.currentThread().getName());
//    }
//
//    // testcmd|testing greet|greeting
//    @Executor(pattern = {"greet", "greeting"}, description = "greets you :O")
//    public BukkitResponse greet(ExamplePlugin diExample, @Inject("testString") String namedDiExample) {
//        return RawResponse.of(this.config.getGreeting(), diExample.getName(), namedDiExample);
//    }
//
//    // testcmd|testing i18n
//    @Executor
//    public Message i18n(CommandSender sender) {
//        return this.i18n.get(sender, this.messages.getPlayerMessage())
//                .with("sender", sender);
//    }
//
//    // testcmd|testing reload
//    @Executor
//    public Message reload(CommandSender sender) {
//
//        try {
//            this.config.load(); // reload config
//            this.i18n.load(); // reload current i18n locales configs (no removing, no adding at runtime)
//        }
//        catch (Exception exception) {
//            this.logger.log(Level.SEVERE, "Failed to reload configuration", exception);
//            return this.i18n.get(sender, this.messages.getCommandsReloadFail());
//        }
//
//        return this.i18n.get(sender, this.messages.getCommandsReloadSuccess());
//    }
//
//    // testcmd|testing i18nbench
//    @Executor
//    public Message i18nbench(CommandSender sender) {
//        Message message = this.i18n.get(sender, this.messages.getPlayerMessage()).with("sender", sender);
//        long start = System.nanoTime();
//        StringBuilder builder = new StringBuilder();
//        for (int i = 0; i < 1000; i++) {
//            builder.append(message.apply());
//        }
//        long took = System.nanoTime() - start;
//        sender.sendMessage(builder.substring(0, 1));
//        sender.sendMessage(took + " " + (took / 1000) );
//        return message;
//    }
//
//    // testcmd|testing i18n <value>
//    @Executor(pattern = "i18n *")
//    public Message i18n(CommandSender sender, @Arg("value") String value) {
//        this.logger.info("test i18n " + value);
//        if ("1".equals(value)) {
//            return this.i18n.get(sender, this.messages.getExampleMessage())
//                    .with("who", 1);
//        } else if ("2".equals(value)) {
//            return this.i18n.get(sender, this.messages.getExampleMessage())
//                    .with("who", 2);
//        } else if ("you".equals(value)) {
//            Message with = this.i18n.get(sender, this.messages.getExampleMessage())
//                    .with("who", "you");
//            this.logger.info(with.apply());
//            return with;
//        } else {
//            return this.i18n.get(sender, this.messages.getExampleMessage());
//        }
//    }
//
//    // testcmd|testing write1000players
//    @Executor(pattern = "writeplayers *", async = true)
//    public void writeplayers(CommandSender sender, @Arg("num") long num) {
//        long start = System.currentTimeMillis();
//        for (int i = 0; i < num; i++) {
//            OfflinePlayer randomPlayer = Bukkit.getOfflinePlayer(UUID.randomUUID());
//            PlayerProperties properties = this.playerPersistence.get(randomPlayer);
//            properties.setName(RandomStringUtils.randomAlphanumeric(8));
//            properties.setLastJoined(Instant.ofEpochMilli(ThreadLocalRandom.current().nextLong(start)));
//            World world = Bukkit.getWorlds().get(ThreadLocalRandom.current().nextInt(Bukkit.getWorlds().size()));
//            int x = ThreadLocalRandom.current().nextInt(20_000);
//            int y = ThreadLocalRandom.current().nextInt(255);
//            int z = ThreadLocalRandom.current().nextInt(20_000);
//            Location location = new Location(world, x, y, z);
//            properties.setLastJoinedLocation(location);
//            properties.save();
//        }
//        long took = System.currentTimeMillis() - start;
//        sender.sendMessage(took + " ms");
//    }
//
//    // testcmd|testing visittest
//    @Executor(async = true)
//    public String visittest() {
//        return this.playerPersistence.streamAll()
//                .filter(entity -> entity.getLastJoinedLocation().getY() > 250)
//                .findFirst()
//                .map(OkaeriConfig::saveToString)
//                .orElse("no match found");
//    }
//
//    // testcmd|testing findplayer <name>
//    @Executor(pattern = "findplayer *", async = true)
//    public String findplayer(@Arg("name") String name) {
//        long start = System.currentTimeMillis();
//        String data = this.playerPersistence.findByName(name)
//                .map(OkaeriConfig::saveToString)
//                .orElse("huh");
//        long took = System.currentTimeMillis() - start;
//        data += "\n" + took + " ms";
//        return data;
//    }
//
//    // testcmd|testing findbyworld <world>
//    @Executor(pattern = "findbyworld *", async = true)
//    public String findbyworld(@Arg("world") String worldName) {
//        long start = System.currentTimeMillis();
//        String data = this.playerPersistence.findByLastJoinedLocationWorld(worldName)
//                .limit(10)
//                .map(OkaeriConfig::saveToString)
//                .collect(Collectors.joining("\n"));
//        long took = System.currentTimeMillis() - start;
//        data += "\n" + took + " ms";
//        return data;
//    }
//
//    // testcmd|testing findbyworld <world>
//    @Executor(pattern = "findbyy *", async = true)
//    public String findbyy(@Arg("world") int y) {
//        long start = System.currentTimeMillis();
//        String data = this.playerPersistence.findByLastJoinedLocationY(y)
//                .limit(10)
//                .map(OkaeriConfig::saveToString)
//                .collect(Collectors.joining("\n"));
//        long took = System.currentTimeMillis() - start;
//        data += "\n" + took + " ms";
//        return data;
//    }
//
//    // testcmd|testing currentthread
//    @Executor(async = true)
//    public String currentthread() {
//        return Thread.currentThread().getName();
//    }
//
//    // testcmd|testing readallplayers
//    @Executor(async = true)
//    public void readallplayers(CommandSender sender) {
//        long start = System.currentTimeMillis();
//        Collection<PlayerProperties> all = this.playerPersistence.findAll();
//        sender.sendMessage(all.stream()
//                .map(properties -> properties.getName() + ": " + properties.getLastJoined())
//                .limit(100)
//                .collect(Collectors.joining("\n")));
//        long took = System.currentTimeMillis() - start;
//        sender.sendMessage(took + " ms (records total: " + all.size() + ")");
//    }
//
//    // testcmd|testing deleteallplayers
//    @Executor(async = true)
//    public void deleteallplayers(CommandSender sender) {
//        long start = System.currentTimeMillis();
//        sender.sendMessage("state: " + this.playerPersistence.deleteAll());
//        long took = System.currentTimeMillis() - start;
//        sender.sendMessage(took + " ms");
//    }
//
//    // testcmd|testing deleteme
//    @Executor(async = true)
//    public void deleteme(@Sender Player player) {
//        long start = System.currentTimeMillis();
//        player.sendMessage("state: " + this.playerPersistence.deleteByPath(player.getUniqueId()));
//        long took = System.currentTimeMillis() - start;
//        player.sendMessage(took + " ms");
//    }
//
//    // testcmd|testing eval <message> - looks dangerous tbh
//    @Executor(pattern = "eval")
//    public String eval(CommandSender sender, @RawArgs List<String> rawArgs) {
//
//        String raw = String.join(" ", rawArgs.subList(1, rawArgs.size()));
//        CompiledMessage message = CompiledMessage.of(raw);
//
//        return this.i18n.getPlaceholders().contextOf(message)
//                .with("sender", sender)
//                .apply();
//    }
//
//    @Executor
//    public String exception() {
//        throw new RuntimeException("rrrrrr");
//    }
//
//    // testcmd|testing tphereall
//    @Executor(async = true)
//    public BukkitResponse tphereall(CommandSender sender) {
//
//        /* waiting for okaeri-commands to use @Sender Player directly - not funny */
//        if (!(sender instanceof Player)) {
//            return ErrorResponse.of("Sorry we cannot do that!");
//        }
//
//        Player player = (Player) sender;
//        Location playerLocation = player.getLocation();
//
//        // example of teleporting multiple players with QueuedTeleports
//        // for simplicity player using the command is teleported too
//        // notice how we use #join() on the future - this operation is blocking
//        // and thus requires executor to be async or scheduler to be used
//        // alternatively if awaiting for all teleports is not required one may
//        // use teleport(Collection<? extends Entity>, Location, TeleportActionCallback) instead
//        this.server.getOnlinePlayers().stream()
//                .map(target -> this.teleports.teleport(target, playerLocation).join())
//                .forEach((target) -> SuccessResponse.of("You have been teleported here by {player}!")
//                        .withField("player", player)
//                        .sendTo(target));
//
//        // respond to the player after everyone is teleported
//        return SuccessResponse.of("Please welcome your new friends!");
//    }
//
//    @Bean("subbean")
//    public String configureExampleSubbean() {
//        return "BEAN FROM " + this.getClass() + "!!";
//    }
//}
