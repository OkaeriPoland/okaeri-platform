# Okaeri Platform (WIP)

![License](https://img.shields.io/github/license/OkaeriPoland/okaeri-platform)
![Total lines](https://img.shields.io/tokei/lines/github/OkaeriPoland/okaeri-platform)
![Repo size](https://img.shields.io/github/repo-size/OkaeriPoland/okaeri-platform)
![Contributors](https://img.shields.io/github/contributors/OkaeriPoland/okaeri-platform)
[![Discord](https://img.shields.io/discord/589089838200913930)](https://discord.gg/hASN5eX)

Whole ecosystems built on top of the best okaeri packages.

- Based on dependency injection/beans/components schema:
  - subcomponent registration with Register(component class) annotation
  - automatic field injections and bean creation
- Persistence abstraction (e.g. BasicFlatPersistence, Cached<T>):
  - serialization/deserialization based on okaeri-configs or custom solution
  - ready to use support for database (redis, mysql, etc.) storage *Coming Soon*™
  - ability to implement custom storage layers easily
- Lots of platform specific utilities:
  - register platform specific services as components
  - use commons for the most boring tasks and cleaner code

## Bukkit

Currently the only target (about 300kB total of additional jar size), integrates:
- **Bukkit Platform Utilities**:
  - `CommandRunner`: run multiple commands with fields on multiple targets (see example)
  - `ItemStackBuilder`: easy item creation/manipulation (see example)
  - `YamlBukkitPersistence`: an easy way to manage e.g. plugin specific player properties (see example)
  - `AllWorldsRunnable`/`OnlinePlayersRunnable`: special runnable for iterating over all players/all worlds
  - `QueuedTeleports`: optimized and elegant way to teleport players (see example)
  - `MinecraftTimeEquivalent`: get ticks approximation from the real world time
- **Dependency Injection**:
  - [okaeri-injector](https://github.com/OkaeriPoland/okaeri-injector): "Probably the most basic and simplest DI possible with just ~9kB in size."
- **Configs**:
  - [okaeri-configs-yaml-bukkit](https://github.com/OkaeriPoland/okaeri-configs/tree/master/yaml-bukkit): config library built on top of the Bukkit's YamlConfiguration (see [okaeri-configs](https://github.com/OkaeriPoland/okaeri-configs))
  - [okaeri-configs-validator-okaeri](https://github.com/OkaeriPoland/okaeri-configs/tree/master/validator-okaeri): simple bean validator (see [okaeri-validator](https://github.com/OkaeriPoland/okaeri-validator))
- **Commands**:
  - [okaeri-commands-bukkit](https://github.com/OkaeriPoland/okaeri-commands/tree/master/bukkit): annotation based command framework (see [okaeri-commands](https://github.com/OkaeriPoland/okaeri-commands))
  - [okaeri-commands-injector](https://github.com/OkaeriPoland/okaeri-commands/tree/master/injector): okaeri-injector integration for the best ecosystem experience
- **Messages (i18n)**
  - [okaeri-i18n](https://github.com/OkaeriPoland/okaeri-i18n): translation library with compile time key checking (getters possible instead of string keys),
    built using amazing [okaeri-placeholders](https://github.com/OkaeriPoland/okaeri-placeholders) with pluralization for 143 locales, subfields, default values, boolean/floating point formatting,
    placeholder arguments (`{player.healthBar(20)}`, `{top.guild(1)}`, `{top.guild(1,kills)}`)

### Example

See [bukkit-example](https://github.com/OkaeriPoland/okaeri-platform/tree/master/bukkit-example) for the repository/dependency and the shading guide. 
Note the code below does not represent full source code of the example. Project in the example directory represents at least basic usage of every component.

```java
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
  // can also be registered as class using @Register
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
  // see usage in the TestTask
  @Bean("cachedDbData")
  public Cached<String> loadDataFromDbWithCache(TestConfig config) {
    // resolves only once at the beginning and then only after ttl expires (using 2nd arg supplier)
    // it is possible to use Cached.of(supplier) to disable ttl completely
    // getting value is done using #get(), it is possible to force update using #update()
    // remember however that if supplier is blocking it would affect your current thread
    return Cached.of(Duration.ofMinutes(1), () -> config.getGreeting() + " [" + Instant.now() + "]");
  }
}
```

```java
@ServiceDescriptor(label = "testcmd", aliases = "testing")
public class TestCommand implements CommandService {

  @Inject("testString") private String test;
  @Inject("exampleComplexBean") private String complexContent;
  @Inject private TestConfig config;

  // testcmd|testing example
  @Executor
  public BukkitResponse example(@Label String label) {
    return SuccessResponse.of("It works! {label} [{test}]")
            .withField("label", label)
            .withField("test", this.test);
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

  @Bean("subbean")
  public String configureExampleSubbean() {
    return "BEAN FROM " + this.getClass() + "!!";
  }
}
```

```java
@Component
public class TestListener implements Listener {

  @Inject private ExamplePlugin plugin;
  @Inject private Logger logger; // plugin's logger (name=logger)
  @Inject private Server server;
  @Inject private QueuedTeleports queuedTeleports;
  @Inject("subbean") private String subbeanString;
  @Inject("joinReward") ItemStack rewardItem;

  @EventHandler
  public void onJoin(PlayerJoinEvent event) {
    event.setJoinMessage("Willkommen " + event.getPlayer().getName() + "! " + this.plugin.getName() + " is working!\n" + this.subbeanString);
    event.getPlayer().getInventory().addItem(this.rewardItem.clone());
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
}
```

```java
// example of timer component class
// async=true - simulating blocking fetching scenario
@Timer(rate = MinecraftTimeEquivalent.MINUTES_5, async = true)
public class TestTask implements Runnable {

  @Inject private TestConfig config;
  @Inject private Server server;
  @Inject private Plugin plugin;

  @Inject("cachedDbData")
  private Cached<String> cachedData;

  @Override
  public void run() {

    // built-in CommandRunner for easy exectution
    // of commands e.g. from the configuration/web/other source
    CommandRunner.of(this.plugin, this.server.getOnlinePlayers()) // accepts any single element or collection
            .forceMainThread(true) // forces execution on the main thread
            .withField("ending", "hmmm..")
            .withField("name", HumanEntity::getName) // dynamic replaces based on current element or static values
            .execute(Arrays.asList("say how are you {name}? {ending}", this.config.getRepeatingCommand())); // pass single element or collection of commands

    // accessing Cached<T>
    String cachedValue = this.cachedData.get();
    Bukkit.broadcastMessage(cachedValue);

    // accessing Cached<T> with forced update
    String updatedValue = this.cachedData.update();
    Bukkit.broadcastMessage(updatedValue);
  }
}
```

```java
// automatically created in the plugin dir
// updates comments and changes (new keys) automatically
// manipulate it as pojo and save with #save()
//
// Resulting file:
// # ================================
// #        Magic Configuration
// # ================================
// # Example config value
// greeting: Hi!!!!!!!!1111oneone
//
// automatically manages file inside plugin's directory
// allows component to be registered with @Register
@Configuration // config.yml by default - to change use path = "custom_location.yml"
// adds header, supports multiline strings or multiple annotations
// string array can be passed as an argument too, same with @Comment
// it is possible to create empty line with "" and empty comment with " "
@Header("================================")
@Header("       Magic Configuration      ")
@Header("================================")
// automatically applies name transformations (by default)
// config keys can be also individually changed using @CustomKey
// strategies: 
// - IDENTITY: do not change (default)
// - SNAKE_CASE: exampleValue -> example_Value
// - HYPHEN_CASE: exampleValue -> example-Value
// modifiers:
// - NONE: do not change (default)
// - TO_LOWER_CASE: e.g. example-Value -> example-value
// - TO_UPPER_CASE: e.g. example_Value -> EXAMPLE_VALUE
@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
public class TestConfig extends OkaeriConfig {

  @Size(min = 1, max = 64) // validation using okaeri-validator
  @Variable("APP_GREETING") // use jvm property or environment variable if available
  @Comment("Example config value") // built-in comment support
  private String greeting = "Hi!!!!!!!!1111oneone"; // default values

  @Comment("Example command")
  private String repeatingCommand = "say from the config for {name}!";

  /* getters/setters or nothing if annotated with lombok */
}
```

### Default injectables

Generally available common instances, useful in almost every component (e.g. Logger):

| Inject Name | Type | Description |
|-|-|-|
| `server` | org.bukkit.`Server` | injectable version of `JavaPlugin#getServer()` |
| `dataFolder` | java.io.`File` | injectable version of `JavaPlugin#getDataFolder()` |
| `logger` | java.util.logging.`Logger` | injectable version of `JavaPlugin#getLogger()` |
| `plugin` | org.bukkit.plugin.`Plugin` | injectable instance of platform plugin |
| `scheduler` | org.bukkit.scheduler.`BukkitScheduler` | injectable version of `Server#getScheduler()` |
| `pluginManager` | org.bukkit.plugin.`PluginManager` | injectable version of `Server#getPluginManager()` |
| `scoreboardManager` | org.bukkit.scoreboard.`ScoreboardManager` | injectable version of `Server#getScoreboardManager()` |

Internals mainly used inside of the platform, but available for manual use. For example Injector can be used to create instances of classes that are using @Inject(s):

| Inject Name | Type | Description |
|-|-|-|
| `commands` | eu.okaeri.commands.`Commands` | instance of `okaeri-commands` used for registering commands internally |
| `injector` | eu.okaeri.injector.`Injector` | instance of `okaeri-injector` used internally |

### Performance

The example plugin loads in under 50ms on the AMD Ryzen 3600 system. Runtime overhead for most of the components
is negligible as most of the work is done at the startup, which is relatively fast, and even less noticeable
thanks to special async preloading technology.

```console
# platform startup speed
[OkaeriPlatformBukkitExample] Enabling OkaeriPlatformBukkitExample v1.0-SNAPSHOT
[OkaeriPlatformBukkitExample] Initializing class org.example.okaeriplatformtest.ExamplePlugin
[OkaeriPlatformBukkitExample] - Added timer: QueuedTeleportsTask { delay = 4, rate = 4, async = false }
[OkaeriPlatformBukkitExample] - Loaded configuration: TestConfig { path = config.yml, provider = DEFAULT } [6 ms]
[OkaeriPlatformBukkitExample] - Loaded messages: TestLocaleConfig { path = i18n, suffix = .yml, provider = DEFAULT } [5 ms]
[OkaeriPlatformBukkitExample]   > es, pl
[OkaeriPlatformBukkitExample] - Added command: TestCommand { label = testcmd, aliases = [testing] }
[OkaeriPlatformBukkitExample] - Added timer: exampleTimer { delay = 1200, rate = 1200, async = true }
[OkaeriPlatformBukkitExample] - Added listener: TestListener { onJoin, onAsyncChat }
[OkaeriPlatformBukkitExample] - Added timer: TestTask { delay = 6000, rate = 6000, async = true }
[OkaeriPlatformBukkitExample] = (configs: 1, commands: 1, listeners: 1, timers: 3, localeConfigs: 2) [27 ms]
```

**Commands (okaeri-commands)**
- Services are scanned at the startup and then saved for further search
- Overhead from matching pattern and method invocation is insignificant, see below

```console
# commands framework overhead benchmark ~ on average about 300 000 invocations per second
# benchmark represents matching/invoking an executor (returning simple string) from multiple available
Benchmark                          Mode  Cnt     Score    Error  Units
BenchmarkCommands.command_complex  avgt   20  4192.172 ± 47.137  ns/op
BenchmarkCommands.command_medium   avgt   20  2321.369 ± 66.198  ns/op
BenchmarkCommands.command_simple   avgt   20  1849.535 ± 23.971  ns/op
```

**Configs (okaeri-configs)**
- At the runtime accessing config object properties is no different from the standard POJO.
- Reading/saving is mostly the same as using bukkit's config directly (I/O is the biggest concern here) and in most cases can be done async.

**Timers (platform's commons)**
- Any timer created as Runnable (@Bean, @Timer (on class)) is no different from manually registering it.
- Method @Timer comes at insignificant cost of the method invocation and DI (best used for async tasks).

**Listeners (platform's commons)**
- Any Listener registered with @Component is no different from manually registering it.
