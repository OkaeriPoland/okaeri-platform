package org.example.okaeriplatformtest;

import eu.okaeri.platform.bukkit.OkaeriPlugin;
import eu.okaeri.platform.bukkit.annotation.Bean;
import eu.okaeri.platform.bukkit.annotation.WithBean;
import org.bukkit.plugin.java.JavaPlugin;

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
