package org.example.okaeriplatformtest.config;

import eu.okaeri.configs.annotation.NameModifier;
import eu.okaeri.configs.annotation.NameStrategy;
import eu.okaeri.configs.annotation.Names;
import eu.okaeri.i18n.configs.LocaleConfig;
import eu.okaeri.platform.core.annotation.Messages;
import lombok.Getter;

@Getter
@Messages // path = "i18n", suffix = ".yml", defaultLocale = "en" # path is used as name of the injectable if multiple locale configurations are present
@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
public class TestLocaleConfig extends LocaleConfig {
    // it is recommended that defaultLocale is implemented as default values here
    private String exampleMessage = "Hello {world,worlds#who|unknown}!";
    private String playerMessage = "Hello {sender.name}!";
    private String reloadMessage = "The configuration has been reloaded!";
    private String reloadFailMessage = "Reload fail! See the console for details.";
}
