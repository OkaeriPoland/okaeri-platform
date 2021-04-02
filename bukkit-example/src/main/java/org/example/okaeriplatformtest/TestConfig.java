package org.example.okaeriplatformtest;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;
import eu.okaeri.platform.bukkit.annotation.Configuration;
import eu.okaeri.validator.annotation.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration(path = "config.yml")
@Header("================================")
@Header("       Magic Configuration      ")
@Header("================================")
@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
public class TestConfig extends OkaeriConfig {

    @Size(min = 1, max = 64) // validation using okaeri-validator
    @Variable("APP_GREETING") // use jvm property environment variable if available
    @Comment("Example config value") // built-in comment support
    private String greeting = "Hi!!!!!!!!1111oneone"; // default values
}
