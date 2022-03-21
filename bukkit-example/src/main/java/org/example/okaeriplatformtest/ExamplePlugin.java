package org.example.okaeriplatformtest;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.zaxxer.hikari.HikariConfig;
import eu.okaeri.commons.bukkit.item.ItemStackBuilder;
import eu.okaeri.commons.bukkit.teleport.QueuedTeleports;
import eu.okaeri.commons.bukkit.teleport.QueuedTeleportsTask;
import eu.okaeri.commons.bukkit.time.MinecraftTimeEquivalent;
import eu.okaeri.commons.cache.Cached;
import eu.okaeri.configs.json.simple.JsonSimpleConfigurer;
import eu.okaeri.configs.yaml.bukkit.serdes.SerdesBukkit;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.persistence.PersistencePath;
import eu.okaeri.persistence.document.DocumentPersistence;
import eu.okaeri.persistence.jdbc.H2Persistence;
import eu.okaeri.persistence.jdbc.MariaDbPersistence;
import eu.okaeri.persistence.mongo.MongoPersistence;
import eu.okaeri.persistence.redis.RedisPersistence;
import eu.okaeri.platform.bukkit.OkaeriBukkitPlugin;
import eu.okaeri.platform.bukkit.annotation.Delayed;
import eu.okaeri.platform.bukkit.annotation.Scheduled;
import eu.okaeri.platform.bukkit.persistence.YamlBukkitPersistence;
import eu.okaeri.platform.core.annotation.Bean;
import eu.okaeri.platform.core.annotation.Scan;
import eu.okaeri.platform.core.plan.ExecutionPhase;
import eu.okaeri.platform.core.plan.Planned;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.example.okaeriplatformtest.config.TestConfig;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

// auto registers beans
// loading starts from the main class
// platform automatically registers:
// - okaeri-commands' CommandService
// - bukkit's Listener (@Component required)
// - okaeri-configs configs' (@Configuration required)
// - Runnables (@Scheduled required)
// - any beans located in class with @Component
@Scan(exclusions = "org.example.okaeriplatformtest.libs", deep = true)
public class ExamplePlugin extends OkaeriBukkitPlugin {

    @Planned(ExecutionPhase.STARTUP) // do not use onEnable (especially without calling super)
    public void onStartup() {
        this.getLogger().info("Enabled!");
    }

    @Planned(ExecutionPhase.SHUTDOWN) // do not use onDisable (especially without calling super)
    public void onShutdown() {
        this.getLogger().info("Disabled!");
    }

    // built-in persistence utils
    // easy storage for e.g. player properties
    // see persistence/PlayerPersistence for details
    @Bean(value = "persistence", preload = true)
    public DocumentPersistence configurePersistence(@Inject("dataFolder") File dataFolder, Plugin plugin, TestConfig config) {

        // jdbc drivers may require initialization for jdbc urls to work
        try { Class.forName("org.mariadb.jdbc.Driver"); } catch (ClassNotFoundException ignored) { }
        try { Class.forName("org.h2.Driver"); } catch (ClassNotFoundException ignored) { }

        // remember that if plugin is not intended to have shared state
        // between multiple instances you must allow users to set persistence's
        // basePath manually or add some other possibility to differ keys
        // otherwise plugin usage would be limited to one instance per one redis
        PersistencePath basePath = PersistencePath.of(config.getStorage().getPrefix());

        // multiple backends are possible with an easy switch
        switch (config.getStorage().getBackend()) {
            case FLAT:
                // specify custom child dir in dataFolder or other custom location
                // or use YamlBukkitPersistence.of(plugin) for default pluginFolder/storage/* (best used for simplest plugins with single storage backend)
                // same as: new DocumentPersistence(new FlatPersistence(new File(dataFolder, "storage"), ".yml"), YamlBukkitConfigurer::new, new SerdesBukkit())
                return YamlBukkitPersistence.of(new File(dataFolder, "storage"));
            case REDIS:
                // construct redis client based on your needs, e.g. using config
                RedisURI redisUri = RedisURI.create(config.getStorage().getUri());
                RedisClient redisClient = RedisClient.create(redisUri);
                // it is HIGHLY recommended to use json configurer for the redis backend
                // other formats may not be supported in the future, json has native support
                // on the redis side thanks to cjson available in lua scripting
                return new DocumentPersistence(new RedisPersistence(basePath, redisClient), JsonSimpleConfigurer::new, new SerdesBukkit());
            case MONGO:
                // construct mongo client based on your needs, e.g. using config
                MongoClientURI mongoUri = new MongoClientURI(config.getStorage().getUri());
                MongoClient mongoClient = new MongoClient(mongoUri);
                // validate if uri contains database
                if (mongoUri.getDatabase() == null) {
                    throw new IllegalArgumentException("Mongo URI needs to specify the database: " + mongoUri.getURI());
                }
                // it is REQUIRED to use json configurer for the mongo backend
                return new DocumentPersistence(new MongoPersistence(basePath, mongoClient, mongoUri.getDatabase()), JsonSimpleConfigurer::new, new SerdesBukkit());
            case MYSQL:
                // setup hikari based on your needs, e.g. using config
                HikariConfig mariadbHikari = new HikariConfig();
                mariadbHikari.setJdbcUrl(config.getStorage().getUri());
                // it is REQUIRED to use json configurer for the mariadb backend
                return new DocumentPersistence(new MariaDbPersistence(basePath, mariadbHikari), JsonSimpleConfigurer::new, new SerdesBukkit());
            case H2:
                // setup hikari based on your needs, e.g. using config
                HikariConfig jdbcHikari = new HikariConfig();
                jdbcHikari.setJdbcUrl(config.getStorage().getUri());
                //it is HIGHLY recommended to use json configurer for the jdbc backend
                return new DocumentPersistence(new H2Persistence(basePath, jdbcHikari), JsonSimpleConfigurer::new, new SerdesBukkit());
            default:
                throw new RuntimeException("unsupported storage backend: " + config.getStorage().getBackend());
        }
    }

    // method beans can use DI
    @Bean("testString")
    public String configureTestString(Plugin plugin) {
        return "plugin -> " + plugin.getName();
    }

    // method bean - remember these are not proxied
    // if TestCommand calls this method using plugin reference
    // it would be executed uncached! @Bean annotation on method
    // is used to instruct the okaeri-platform system to invoke
    // it then register bean/subbeans components and injectable
    @Bean("exampleComplexBean")
    public String configureComplexBean() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            builder.append(i).append(". hi").append("-");
        }
        return builder.toString();
    }

    // magic bean for shared counter
    // available from the other classes as inject too
    // does not require passing plugin instance
    // and can be used directly by the name
    @Bean("exampleCounter")
    public AtomicInteger configureCounter() {
        return new AtomicInteger();
    }

    // scheduled with injected properties
    // supports all bukkit scheduler features:
    // delay: time before first call (defaults to same as rate)
    // rate: time between executions (in ticks)
    // async: should runTaskTimerAsynchronously be used
    @Scheduled(rate = MinecraftTimeEquivalent.MINUTE, async = true)
    public void exampleTimer(TestConfig config, @Inject("exampleCounter") AtomicInteger counter) {
        Bukkit.broadcastMessage(config.getGreeting() + " [" + counter.getAndIncrement() + "]");
    }

    // built-in teleport optimization
    // uses async teleportation if available
    // and queues teleports limiting potential lag spikes
    // see more in the TestListener for usage
    @Bean("teleportsQueue")
    public QueuedTeleports configureQueuedTeleports() {
        return new QueuedTeleports(this);
    }

    // QueuedTeleports requires a task to be registered manually for fine control
    // this also demonstrates using @Scheduled with classes implementing Runnable
    // SECONDS_1/5 = 20/4 = 4 ticks = tries to teleport 1 player (3rd argument) every 4 ticks
    // it is always recommended to decrease rate before increasing teleportsPerRun
    @Scheduled(rate = MinecraftTimeEquivalent.SECOND / 5)
    public QueuedTeleportsTask configureTeleportsTask(QueuedTeleports teleports) {
        return new QueuedTeleportsTask(teleports, this, 1);
    }

    // built-in itemstack manipulation commons
    // in this case we use bean for clarity and
    // for the demo purposes but if object
    // is used locally only, it can be created
    // in the field directly = no mess in DI
    @Bean("joinReward")
    public ItemStack configureRewardItem() {
        return ItemStackBuilder.of(Material.DIAMOND, 1)
                .withName("&bDiamond") // name (auto color) or nameRaw
                .withLore("&fWoah!") // lore or loreRaw, lists or single, appendLore or appendLoreRaw
                .withEnchantment(Enchantment.DURABILITY, 10) // add enchantments
                .withFlag(ItemFlag.HIDE_ENCHANTS) // add magic flag
                .makeUnbreakable() // wow no breaking :O
                .manipulate((item) -> item) // manipulate item manually without breaking out of the builder
                .get(); // gotta resolve that stack
    }

    // built-in cache abstraction
    // se usage in the TestTask
    @Bean("cachedDbData")
    public Cached<String> loadDataFromDbWithCache(TestConfig config) {
        // resolves only once at the beginning and then only after ttl expires (using 2nd arg supplier)
        // it is possible to use Cached.of(supplier) to disable ttl completely (or preferably Lazy.of(supplier)
        // getting value is done using #get(), it is possible to force update using #update()
        // remember however that if supplier is blocking it would affect your current thread
        return Cached.of(Duration.ofMinutes(1), () -> config.getGreeting() + " [" + Instant.now() + "]");
    }

    // run methods scheduled delayed after full server startup
    // useful for update notifications and other similar tasks
    @Delayed(time = MinecraftTimeEquivalent.SECOND, async = true)
    public void runAfterServerIsFullyLoaded() {
        this.getLogger().info("Looks like server is now fully loaded!");
    }
}
