package eu.okaeri.platform.minecraft.i18n;

import eu.okaeri.configs.annotation.NameModifier;
import eu.okaeri.configs.annotation.NameStrategy;
import eu.okaeri.configs.annotation.Names;
import eu.okaeri.i18n.configs.LocaleConfig;
import eu.okaeri.platform.core.annotation.Messages;

@Messages(path = "i18n-platform-commands", unpack = false)
@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
public class I18nCommandsMessages extends LocaleConfig {
    String commandsSystemUsageTemplate;
    String commandsSystemUsageEntry;
    String commandsSystemUsageEntryDescription;
    String commandsSystemPermissionsError;
    String commandsSystemCommandError;
    String commandsSystemUnknownError;
    String commandsSystemConsoleOnlyError;
    String commandsSystemPlayerOnlyError;
}
