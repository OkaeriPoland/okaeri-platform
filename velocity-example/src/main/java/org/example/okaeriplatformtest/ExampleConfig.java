package org.example.okaeriplatformtest;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.platform.core.annotation.Configuration;
import lombok.Getter;


@Getter
@Configuration
public class ExampleConfig extends OkaeriConfig {
    private String motd = "Hello there!";
}
