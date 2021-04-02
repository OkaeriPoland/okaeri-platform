# Okaeri Platform (WIP)

Whole ecosystems built on top of the best okaeri packages.

## Bukkit

Currently the only target, integrates:

- **Dependency Injection**:
  - [okaeri-injector](https://github.com/OkaeriPoland/okaeri-injector): "Probably the most basic and simplest DI possible with just ~9kB in size."
- **Configs**:
  - [okaeri-configs-yaml-bukkit](https://github.com/OkaeriPoland/okaeri-configs/tree/master/yaml-bukkit): config library built on top of the Bukkit's YamlConfiguration (see [okaeri-configs](https://github.com/OkaeriPoland/okaeri-configs))
  - [okaeri-configs-validator-okaeri](https://github.com/OkaeriPoland/okaeri-configs/tree/master/validator-okaeri): simple bean validator based on [okaeri-validator](https://github.com/OkaeriPoland/okaeri-validator)
- **Commands**:
  - [okaeri-commands-bukkit](https://github.com/OkaeriPoland/okaeri-commands/tree/master/bukkit): annotation based command framework, see [okaeri-commands](https://github.com/OkaeriPoland/okaeri-commands)
  - [okaeri-commands-injector](https://github.com/OkaeriPoland/okaeri-commands/tree/master/injector): okaeri-injector integration for the best ecosystem experience

### Example

See [bukkit-example](https://github.com/OkaeriPoland/okaeri-platform/tree/master/bukkit-example) for the repository/dependency and the shading guide.

```java
@WithBean(TestCommand.class)
@WithBean(TestListener.class)
public class ExamplePlugin extends OkaeriPlugin {

    @Override
    public void onPlatformEnabled() {
        System.out.println("enabled!");
    }

    @Override
    public void onPlatformDisabled() {
        System.out.println("disabled!");
    }

    @Bean(value = "testString", register = false)
    public String configureTestString(JavaPlugin plugin) {
        return "plugin -> " + plugin.getName();
    }

    @Bean("exampleComplexBean")
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

    @Executor
    public BukkitResponse example(@Label String label) {
        return SuccessResponse.of("It works! " + label + " [" + this.test + "]");
    }

    @Executor
    public BukkitResponse complex() {
        return RawResponse.of(this.complexContent);
    }

    @Bean(value = "subbean", register = false)
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
