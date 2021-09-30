package org.example.okaeriplatformtest.config;

import eu.okaeri.configs.annotation.NameModifier;
import eu.okaeri.configs.annotation.NameStrategy;
import eu.okaeri.configs.annotation.Names;
import eu.okaeri.i18n.configs.LocaleConfig;
import eu.okaeri.platform.core.annotation.Messages;
import lombok.Getter;

@Getter
// path is used as name of the injectable if multiple locale configurations are present it is also the location in plugin's directory/resources
// suffix is the resulting file suffix and search pattern for the additional locales
// defaultLocale is a fallback locale and the assumed locale of the default field values
// unpack defines should files present in the {path} of the plugin's resources should be written to the plugin's directory
@Messages // path = "i18n", suffix = ".yml", defaultLocale = "en", unpack = true
@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
public class TestLocaleConfig extends LocaleConfig {
    // it is HIGHLY recommended that defaultLocale is implemented as default values here
    private String pageTitle = "Hello {world,worlds#who|unknown}!"; // pluralization and default values: for details see okaeri-i18n
    private String pageMessage = "Hello {user.name}!"; // accessing subfields of placeholders: for details see okaeri-i18n
}
