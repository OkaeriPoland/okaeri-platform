# Okaeri Platform (WIP)

![License](https://img.shields.io/github/license/OkaeriPoland/okaeri-platform)
![Total lines](https://img.shields.io/tokei/lines/github/OkaeriPoland/okaeri-platform)
![Repo size](https://img.shields.io/github/repo-size/OkaeriPoland/okaeri-platform)
![Contributors](https://img.shields.io/github/contributors/OkaeriPoland/okaeri-platform)
[![Discord](https://img.shields.io/discord/589089838200913930)](https://discord.gg/hASN5eX)

Whole ecosystems built on top of the best okaeri packages.

## Bukkit

Currently the only target (about 200kB total of additional jar size), integrates:

- **Dependency Injection**:
  - [okaeri-injector](https://github.com/OkaeriPoland/okaeri-injector): "Probably the most basic and simplest DI possible with just ~9kB in size."
- **Configs**:
  - [okaeri-configs-yaml-bukkit](https://github.com/OkaeriPoland/okaeri-configs/tree/master/yaml-bukkit): config library built on top of the Bukkit's YamlConfiguration (see [okaeri-configs](https://github.com/OkaeriPoland/okaeri-configs))
  - [okaeri-configs-validator-okaeri](https://github.com/OkaeriPoland/okaeri-configs/tree/master/validator-okaeri): simple bean validator (see [okaeri-validator](https://github.com/OkaeriPoland/okaeri-validator))
- **Commands**:
  - [okaeri-commands-bukkit](https://github.com/OkaeriPoland/okaeri-commands/tree/master/bukkit): annotation based command framework (see [okaeri-commands](https://github.com/OkaeriPoland/okaeri-commands))
  - [okaeri-commands-injector](https://github.com/OkaeriPoland/okaeri-commands/tree/master/injector): okaeri-injector integration for the best ecosystem experience
- **Messages (i18n)**
  - *Coming Soon*™: currently it is recommended to create `@Configuration(path = "messages.yml")`

### Example

See [bukkit-example](https://github.com/OkaeriPoland/okaeri-platform/tree/master/bukkit-example) for the repository/dependency and the shading guide.

```java
// auto registers beans
// loading starts from the main class
// platform automatically registers:
// - okaeri-commands' CommandService
// - bukkit's Listener (@Component required)
// - okaeri-configs configs' (@Configuration required)
// - any beans located in class with @Component
// skip registration using register=false
@Register(TestConfig.class)
@Register(TestCommand.class)
@Register(TestListener.class)
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
  public String configureTestString(JavaPlugin plugin) {
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
    return SuccessResponse.of("It works! " + label + " [" + this.test + "]");
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
  @Inject("subbean") private String subbeanString;
  @Inject("joinReward") ItemStack rewardItem;
  @Inject private QueuedTeleports queuedTeleports;

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
      Location spawnLocation = this.plugin.getServer().getWorlds().get(0).getSpawnLocation();
      this.queuedTeleports.teleport(event.getPlayer(), spawnLocation);
      return;
    }

    if (event.getMessage().contains("can i have fly")) {
      // you can use callback paramter if you want to make sure
      // actions are being executed only after teleportation happened
      Location locationTenUp = event.getPlayer().getLocation().add(0, 10, 0);
      this.queuedTeleports.teleport(event.getPlayer(), locationTenUp, (player) -> player.sendMessage("Enjoy flying!"));
    }
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
@Configuration(path = "config.yml")
@Header("================================")
@Header("       Magic Configuration      ")
@Header("================================")
@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
public class TestConfig extends OkaeriConfig {

  @Size(min = 1, max = 64) // validation using okaeri-validator
  @Variable("APP_GREETING") // use jvm property or environment variable if available
  @Comment("Example config value") // built-in comment support
  private String greeting = "Hi!!!!!!!!1111oneone"; // default values

  /* getters/setters or nothing if annotated with lombok */
}
```
