package org.example.okaeriplatformtest;

import eu.okaeri.configs.json.simple.JsonSimpleConfigurer;
import eu.okaeri.configs.yaml.bukkit.serdes.SerdesBukkit;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.platform.bukkit.OkaeriBukkitPlugin;
import eu.okaeri.platform.bukkit.annotation.Timer;
import eu.okaeri.platform.bukkit.commons.item.ItemStackBuilder;
import eu.okaeri.platform.bukkit.commons.persistence.YamlBukkitPersistence;
import eu.okaeri.platform.bukkit.commons.teleport.QueuedTeleports;
import eu.okaeri.platform.bukkit.commons.teleport.QueuedTeleportsTask;
import eu.okaeri.platform.bukkit.commons.time.MinecraftTimeEquivalent;
import eu.okaeri.platform.core.annotation.Bean;
import eu.okaeri.platform.core.annotation.Register;
import eu.okaeri.platform.persistence.PersistencePath;
import eu.okaeri.platform.persistence.cache.Cached;
import eu.okaeri.platform.persistence.config.ConfigPersistence;
import eu.okaeri.platform.persistence.redis.BasicRedisPersistence;
import io.lettuce.core.RedisClient;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.example.okaeriplatformtest.config.TestConfig;
import org.example.okaeriplatformtest.config.TestLocaleConfig;
import org.example.okaeriplatformtest.persistence.PlayerPersistence;

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
// - Runnables (@Timer required)
// - any beans located in class with @Component
// skip registration using register=false
@Register(TestConfig.class)
@Register(TestLocaleConfig.class)
@Register(PlayerPersistence.class)
@Register(TestCommand.class)
@Register(TestListener.class)
@Register(TestTask.class)
public class ExamplePlugin extends OkaeriBukkitPlugin {

    @Inject("subbean")
    private String subbeanStr;

    @Override // do not use onEnable (especially without calling super)
    public void onPlatformEnabled() {
        this.getLogger().info("Enabled!");
    }

    @Override // do not use onDisable (especially without calling super)
    public void onPlatformDisabled() {
        this.getLogger().info("Disabled!");
    }

    // built-in persistence utils
    // easy storage for e.g. player properties
    // see persistence/PlayerPersistence for details
    @Bean("persistence")
    public ConfigPersistence configurePersistence(@Inject("dataFolder") File dataFolder, Plugin plugin, TestConfig config) {
        switch (config.getStorageBackend()) {
            case FLAT:
                // specify custom child dir in dataFolder or other custom location
                // or use YamlBukkitPersistence.of(plugin) for default pluginFolder/storage/* (best used for simplest plugins with single storage backend)
                // same as: new BasicFlatPersistence(new File(dataFolder, "storage"), ".yml", new YamlBukkitConfigurer(), new SerdesBukkit())
                return YamlBukkitPersistence.of(new File(dataFolder, "storage"));
            case REDIS:
                // multiple backends are possible with an easy switch
                // remember that if plugin is not intended to have shared state
                // between multiple instances you must allow users to set persistence's
                // basePath manually or add some other possibility to differ keys
                // otherwise plugin usage would be limited to one instance per one redis
                PersistencePath basePath = PersistencePath.of(config.getStoragePrefix());
                // construct redis client based on your needs, e.g. using config
                RedisClient redisClient = RedisClient.create(config.getStorageRedisUri());
                // it is recommended to use json configurer for the redis backend
                return new BasicRedisPersistence(basePath, redisClient, new JsonSimpleConfigurer(), new SerdesBukkit());
            default:
                throw new RuntimeException("unsupported storage backend: " + config.getStorageBackend());
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

    // timer with injected properties
    // supports all bukkit scheduler features:
    // delay: time before first call (defaults to same as rate)
    // rate: time between executions (in ticks)
    // async: should runTaskTimerAsynchronously be used
    @Timer(rate = MinecraftTimeEquivalent.MINUTES_1, async = true)
    public void exampleTimer(TestConfig config, @Inject("exampleCounter") AtomicInteger counter) {
        Bukkit.broadcastMessage(config.getGreeting() + " [" + counter.getAndIncrement() + "]");
    }

    // built-in teleport optimization
    // uses async teleportation if available
    // and queues teleports limiting potential lag spikes
    // see more in the TestListener for usage
    @Bean("teleportsQueue")
    public QueuedTeleports configureQueuedTeleports() {
        return new QueuedTeleports();
    }

    // QueuedTeleports requires a task to be registered manually for fine control
    // this also demonstrates using @Timer with classes implementing Runnable
    // SECONDS_1/5 = 20/4 = 4 ticks = tries to teleport 1 player (3rd argument) every 4 ticks
    // it is always recommended to decrease rate before increasing teleportsPerRun
    @Timer(rate = MinecraftTimeEquivalent.SECONDS_1 / 5)
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
        // it is possible to use Cached.of(supplier) to disable ttl completely
        // getting value is done using #get(), it is possible to force update using #update()
        // remember however that if supplier is blocking it would affect your current thread
        return Cached.of(Duration.ofMinutes(1), () -> config.getGreeting() + " [" + Instant.now() + "]");
    }
}
