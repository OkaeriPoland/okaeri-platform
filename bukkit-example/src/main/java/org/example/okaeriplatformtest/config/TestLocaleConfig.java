package org.example.okaeriplatformtest.config;

import eu.okaeri.configs.annotation.NameModifier;
import eu.okaeri.configs.annotation.NameStrategy;
import eu.okaeri.configs.annotation.Names;
import eu.okaeri.i18n.configs.LocaleConfig;
import eu.okaeri.platform.core.annotation.Messages;
import lombok.Getter;

@Getter
@Messages(defaultLocale = "en")
@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
public class TestLocaleConfig extends LocaleConfig {
    private String exampleMessage = "Hello {world,worlds#who|unknown}!";
    private String playerMessage = "Hello {sender.name}!";
}
