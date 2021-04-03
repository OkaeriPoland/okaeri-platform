# Okaeri Platform (WIP)

![License](https://img.shields.io/github/license/OkaeriPoland/okaeri-platform)
![Total lines](https://img.shields.io/tokei/lines/github/OkaeriPoland/okaeri-platform)
![Repo size](https://img.shields.io/github/repo-size/OkaeriPoland/okaeri-platform)
![Contributors](https://img.shields.io/github/contributors/OkaeriPoland/okaeri-platform)
[![Discord](https://img.shields.io/discord/589089838200913930)](https://discord.gg/hASN5eX)

Whole ecosystems built on top of the best okaeri packages.

## Bukkit

Currently the only target (less than 200kB total of additional jar size), integrates:

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
// warning: currently there is no smart detection
// loading order:
// - method beans (eg. used for mysql connector)
// - beans added using WithBean annotation
// beans are inspected for subbeans by default
// loading starts from the main class
// platform automatically registers:
// - okaeri-commands' CommandService
// - bukkit's Listener
// - okaeri-configs configs' (@Configuration required)
// skip registration using register=false
// skip scanning for subbeans using scan=false
@WithBean(TestConfig.class)
@WithBean(TestCommand.class)
@WithBean(TestListener.class)
public class ExamplePlugin extends OkaeriBukkitPlugin {

  @Override // do not use onEnable (especially without calling super)
  public void onPlatformEnabled() {
    this.getLogger().info("Enabled!");
  }

  @Override // do not use onDisable (especially without calling super)
  public void onPlatformDisabled() {
    this.getLogger().info("Disabled!");
  }

  @Bean(value = "testString", scan = false)
  public String configureTestString(JavaPlugin plugin) {
    return "plugin -> " + plugin.getName();
  }

  // method bean - remember these are not proxied
  // if TestCommand calls this method using plugin reference
  // it would be executed uncached! @Bean annotation on method
  // is used to instruct the okaeri-platform system to invoke
  // it then register bean/subbeans components and injectable
  @Bean(value = "exampleComplexBean", scan = false)
  public String configureComplexBean() {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < 10; i++) {
      builder.append(i).append(". hi").append("\n");
    }
    return builder.toString();
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

  @Bean(value = "subbean", scan = false)
  public String configureExampleSubbean() {
    return "BEAN FROM " + this.getClass() + "!!";
  }
}
```

```java
public class TestListener implements Listener {

  @Inject private ExamplePlugin plugin;
  @Inject("subbean") private String subbeanString;

  @EventHandler
  public void onJoin(PlayerJoinEvent event) {
    event.setJoinMessage("Willkommen " + event.getPlayer().getName() + "! " + this.plugin.getName() + " is working!\n" + this.subbeanString);
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
