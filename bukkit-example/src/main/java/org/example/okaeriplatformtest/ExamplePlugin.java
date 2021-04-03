package org.example.okaeriplatformtest;

import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.platform.bukkit.OkaeriBukkitPlugin;
import eu.okaeri.platform.core.annotation.Bean;
import eu.okaeri.platform.core.annotation.Register;
import org.bukkit.plugin.java.JavaPlugin;

// auto registers beans
// warning: currently there is no smart detection
// loading order:
// - method beans (eg. used for mysql connector)
// - beans added using With annotation
// beans are inspected for subbeans by default
// loading starts from the main class
// platform automatically registers:
// - okaeri-commands' CommandService
// - bukkit's Listener
// - okaeri-configs configs' (@Configuration required)
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
    @Bean(value = "testString")
    public String configureTestString(JavaPlugin plugin) {
        return "plugin -> " + plugin.getName();
    }

    // method bean - remember these are not proxied
    // if TestCommand calls this method using plugin reference
    // it would be executed uncached! @Bean annotation on method
    // is used to instruct the okaeri-platform system to invoke
    // it then register bean/subbeans components and injectable
    @Bean(value = "exampleComplexBean")
    public String configureComplexBean() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            builder.append(i).append(". hi").append("-");
        }
        return builder.toString();
    }
}
