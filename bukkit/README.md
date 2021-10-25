# Okaeri Platform (WIP) | Bukkit (1.8+)


- **Utilities**:
    - `PlatformScheduler`: alternative to BukkitScheduler with simplified methods and no plugin argument in methods
    - `YamlBukkitPersistence`: an easy way to manage e.g. plugin specific player properties (see example)
    - [okaeri-commons](https://github.com/OkaeriPoland/okaeri-commons) (core + bukkit):
        - `CommandRunner`: run multiple commands with fields on multiple targets (see example)
        - `ItemStackBuilder`: easy item creation/manipulation (see example)
        - `AllWorldsRunnable`/`OnlinePlayersRunnable`: special runnable for iterating over all players/all worlds
        - `QueuedTeleports`: optimized and elegant way to teleport players (see example)
        - `MinecraftTimeEquivalent`: get ticks approximation from the real world time
        - `CacheMap`, `Cached`, `Enums`, `Numbers`, `Strings` and others
    - [okaeri-tasker](https://github.com/OkaeriPoland/okaeri-tasker):
        - `Tasker`: fluent api for BukkitScheduler `tasker.newChain().async(() -> "hi").acceptSync((msg) -> ...).execute()` (see example)
- **Dependency Injection**:
    - [okaeri-injector](https://github.com/OkaeriPoland/okaeri-injector): "Probably the most basic and simplest DI possible with just ~13kB in size."
- **Configs**:
    - [okaeri-configs-yaml-bukkit](https://github.com/OkaeriPoland/okaeri-configs/tree/master/yaml-bukkit): config library built on top of the Bukkit's YamlConfiguration (see [okaeri-configs](https://github.com/OkaeriPoland/okaeri-configs))
    - [okaeri-configs-validator-okaeri](https://github.com/OkaeriPoland/okaeri-configs/tree/master/validator-okaeri): simple bean validator (see [okaeri-validator](https://github.com/OkaeriPoland/okaeri-validator))
- **Commands**:
    - [okaeri-commands-bukkit](https://github.com/OkaeriPoland/okaeri-commands/tree/master/bukkit): annotation based command framework with automatic brigadier (1.13+ tab complete) support (see [okaeri-commands](https://github.com/OkaeriPoland/okaeri-commands))
    - [okaeri-commands-injector](https://github.com/OkaeriPoland/okaeri-commands/tree/master/injector): okaeri-injector integration for the best ecosystem experience
- **Messages (i18n)**
    - [okaeri-i18n](https://github.com/OkaeriPoland/okaeri-i18n): translation library with compile time key checking (getters possible instead of string keys),
      built using amazing [okaeri-placeholders](https://github.com/OkaeriPoland/okaeri-placeholders) with pluralization for 143 locales, subfields, default values, boolean/floating point formatting,
      placeholder arguments (`{player.healthBar(20)}`, `{top.guild(1)}`, `{top.guild(1,kills)}`)
- **Persistence**
    - [okaeri-persistence](https://github.com/OkaeriPoland/okaeri-persistence): Object Document Mapping (ODM) library allowing to focus on data instead of the storage layer. Integrates seamlessly
      with configuration (okaeri-configs) objects and allows to store, index, and filter them with almost no additional work needed. Supports multiple backends like files, mysql, redis with just an easy switch.

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
// - Runnables (@Scheduled required)
// - any beans located in class with @Component
// skip registration using register=false
@Register(TestConfig.class)
@Register(TestLocaleConfig.class)
@Register(TestCommand.class)
@Register(TestListener.class)
@Register(TestTask.class)
public class ExamplePlugin extends OkaeriBukkitPlugin {

  @Inject("subbean")
  private String subbeanStr;

  @Planned(ExecutionPhase.STARTUP) // do not use onEnable (especially without calling super)
  public void onStartup() {
    this.getLogger().info("Enabled!");
  }

  @Planned(ExecutionPhase.SHUTDOWN) // do not use onDisable (especially without calling super)
  public void onShutdown() {
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

  // scheduled with injected properties
  // can also be registered as class using @Register
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
  // see usage in the TestTask
  @Bean("cachedDbData")
  public Cached<String> loadDataFromDbWithCache(TestConfig config) {
    // resolves only once at the beginning and then only after ttl expires (using 2nd arg supplier)
    // it is possible to use Cached.of(supplier) to disable ttl completely (or preferably Lazy.of(supplier)
    // getting value is done using #get(), it is possible to force update using #update()
    // remember however that if supplier is blocking it would affect your current thread
    return Cached.of(Duration.ofMinutes(1), () -> config.getGreeting() + " [" + Instant.now() + "]");
  }
}
```

```java
// @Async
// @Permission("testcmd.admin")
@Command(label = "testcmd", aliases = "testing")
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
  @Async // or @Async on class and @Sync on methods
  @Executor(description = "wow async execution, db calls go brrr")
  public BukkitResponse complex() {
    return RawResponse.of(this.complexContent, Thread.currentThread().getName());
  }

  // testcmd|testing chains
  //
  // @Chain annotation is available
  // in the command methods only
  //
  // for other places use @Inject Tasker
  // and create chain manually
  //
  public TaskerChain<?> chains(@Chain("name"/* or empty if not queued */) TaskerChain<?> chain) {
    return chain
      .async(() -> {
        String threadName = Thread.currentThread().getName();
        return "some async data from " + threadName;
      })
      .acceptSync((data) -> {
        String threadName = Thread.currentThread().getName();
        Bukkit.broadcastMessage("received: " + data + " in " + threadName);
      }); // do not #execute() if returning, will be executed by platform
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
// example of scheduled component class
// async=true - simulating blocking fetching scenario
@Scheduled(rate = MinecraftTimeEquivalent.MINUTES * 5, async = true)
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

```java
// path is used as name of the injectable if multiple locale configurations are present it is also the location in plugin's directory/resources
// suffix is the resulting file suffix and search pattern for the additional locales
// defaultLocale is a fallback locale and the assumed locale of the default field values
// unpack defines should files present in the {path} of the plugin's resources should be written to the plugin's directory
@Messages // path = "i18n", suffix = ".yml", defaultLocale = "en", unpack = true
@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
public class TestLocaleConfig extends LocaleConfig {
    // it is HIGHLY recommended that defaultLocale is implemented as default values here
    private String exampleMessage = "Hello {world,worlds#who|unknown}!"; // pluralization and default values: for details see okaeri-i18n
    private String playerMessage = "Hello {sender.name}!"; // accessing subfields of placeholders: for details see okaeri-i18n
    // it is advised to leave messages uncolored and use key based coloring
    // keys ending with: fail, failed result in &c colored message
    // keys ending with: success, succeeded result in &a colored message
    // any other keys with uncolored messages result in &e colored message
    // for details and defining custom patterns see minecraft/I18N_COLORS.md
    // you can define such file same way as locales in the plugin's resources
    // e.g. {path}/es.yml, {path}/colors.yml, or use default values (recommended)
    private String commandsReloadSuccess = "The configuration has been reloaded!"; // same as: &aThe configuration has been reloaded!
    private String commandsReloadFail = "Reload fail! See the console for details."; // same as: &cReload fail! See the console for details.
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
| `scheduler` | eu.okaeri.platform.bukkit.scheduler.`PlatformScheduler` | custom, simplified BukkitScheduler alternative |
| `tasker` | eu.okaeri.tasker.core.`Tasker` | chaining runnable/consumer/supplier/function executor abstraction for BukkitScheduler |
| `pluginManager` | org.bukkit.plugin.`PluginManager` | injectable version of `Server#getPluginManager()` |
| `scoreboardManager` | org.bukkit.scoreboard.`ScoreboardManager` | injectable version of `Server#getScoreboardManager()` |

Internals mainly used inside of the platform, but available for manual use. For example Injector can be used to create instances of classes that are using @Inject(s):

| Inject Name | Type | Description |
|-|-|-|
| `commands` | eu.okaeri.commands.`Commands` | instance of `okaeri-commands` used for registering commands internally |
| `injector` | eu.okaeri.injector.`Injector` | instance of `okaeri-injector` used internally |

Overridable defaults used in various components:

```java
// example of i18nLocaleProvider override (in plugin's main class)
@Planned(ExecutionPhase.SETUP)
public void setup(Injector injector) {
    injector.registerExclusive("i18nLocaleProvider", new MyLocaleProvider());
}
```

| Inject Name | Type | Description |
|-|-|-|
| `defaultConfigurerProvider` | eu.okaeri.persistence.document.`ConfigurerProvider` | default configuration provider for @Configuration, @Messages and others |
| `defaultConfigurerSerdes` | Class[] | list of default OkaeriSerdesPack(s) classes to be used with `defaultConfigurerProvider` |
| `defaultPlaceholdersFactory` | eu.okaeri.platform.core.placeholder.`DefaultPlaceholdersFactory` | default placeholders provider to be used e.g. in i18n |
| `i18nLocaleProvider` | eu.okaeri.i18n.provider.`LocaleProvider` | platform's locale provider for i18n components e.g. commands |

### Performance

The example plugin with flat persistence loads in about 250 ms on the AMD Ryzen 3600 system. Runtime overhead for
most of the components is negligible as most of the work is done at the startup, which is relatively fast.

### Startup logs
```console
# platform startup speed
[..] Enabling OkaeriPlatformBukkitExample v1.0-SNAPSHOT
[..] Loading ExamplePlugin
[..] - Added generic bean: joinReward [24 ms]
[..] - Added scheduled: configureTeleportsTask { async = false, delay = 4, rate = 4 } [0 ms]
[..] - Added delayed: runAfterServerIsFullyLoaded { async = true, time = 20 } [0 ms]
[..] - Loaded configuration: TestConfig { path = 'config.yml', provider = 'DEFAULT' } [42 ms]
[..] - Loaded messages: TestLocaleConfig { path = 'i18n', provider = 'DEFAULT', suffix = '.yml' } [21 ms]
[..]   > en, es
[..] - Added command: TestPreloadedCommand { aliases = [testing], description = '', label = 'testpcmd' } [9 ms]
[..] - Added generic bean: persistence [9 ms]
[..] - Added scheduled: exampleTimer { async = true, delay = 1200, rate = 1200 } [0 ms]
[..] - Added persistence repository: PlayerRepository { dependsOn = 'persistence->DocumentPersistence' } [19 ms]
[..] - Added command: TestCommand { aliases = [testing], description = '', label = 'testcmd' } [4 ms]
[..] - Added listener: TestListener { methods = [onCommandsUnknownError, onAsyncChat, onJoin] } [2 ms]
[..] - Added scheduled: TestTask { async = true, delay = 6000, rate = 6000 } [0 ms]
[..] = (beans: 8, commands: 2, configs: 1, delayed: 1, listeners: 1, localeConfigs: 11, persistenceRepositories: 1, scheduled: 3) [blocking: 247 ms]
```

**Commands (okaeri-commands)**
- Services are scanned at the startup and then saved for further search
- Overhead from matching pattern and method invocation is insignificant, see below

```console
# commands framework overhead benchmark ~ over 1 000 000 invocations per second
# benchmark represents matching/invoking an executor (returning simple string) from multiple available
Benchmark                           Mode  Cnt        Score       Error  Units
BenchmarkCommands.command_complex  thrpt    5  1109842.766 ± 47168.401  ops/s   901 ns/invocation
BenchmarkCommands.command_medium   thrpt    5  1112048.204 ± 46700.652  ops/s   899 ns/invocation
BenchmarkCommands.command_simple   thrpt    5  1253307.288 ± 31533.820  ops/s   797 ns/invocation
```

**Configs (okaeri-configs)**
- At the runtime accessing config object properties is no different from the standard POJO.
- Reading/saving is mostly the same as using bukkit's config directly (I/O is the biggest concern here) and in most cases can be done async.

**Timers (platform's commons)**
- Any timer created as Runnable (@Bean, @Scheduled (on class)) is no different from manually registering it.
- Method @Scheduled comes at insignificant cost of the method invocation and DI (best used for async tasks).

**Listeners (platform's commons)**
- Any Listener registered with @Component is no different from manually registering it.