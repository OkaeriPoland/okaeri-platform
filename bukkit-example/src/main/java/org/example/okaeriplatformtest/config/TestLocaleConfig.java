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
public class TestLocaleConfig extends LocaleConfig {
    // it is HIGHLY recommended that defaultLocale is implemented as default values here
    private String exampleMessage = "Hello {world,worlds#who|unknown}!"; // pluralization and default values: for details see okaeri-i18n
    private String playerMessage = "Hello {sender.name}!"; // accessing subfields of placeholders: for details see okaeri-i18n
    // it is advised to leave messages uncolored and use key based coloring
    // keys ending with: fail, failed result in &c colored message
    // keys ending with: success, succeeded result in &a colored message
    // any other keys with uncolored messages result in &e colored message
    // for details and defining custom patterns see minecraft/I18N_COLORS.md
    // you can define such file same way as locales in the plugin's resources
    // e.g. {path}/es.yml, {path}/colors.yml, or use default values (recommended)
    private String commandsReloadSuccess = "The configuration has been reloaded!"; // same as: &aThe configuration has been reloaded!
    private String commandsReloadFail = "Reload fail! See the console for details."; // same as: &cReload fail! See the console for details.
}
