package eu.okaeri.platform.minecraft.i18n;

import eu.okaeri.i18n.configs.LocaleConfig;
import eu.okaeri.platform.core.annotation.Messages;

@Messages(path = "i18n-platform-commands", unpack = false)
public class I18nCommandsMessages extends LocaleConfig {
    String commandSystemUsageTemplate;
    String commandSystemUsageEntry;
    String commandSystemUsageEntryDescription;
    String commandSystemPermissionsError;
    String commandSystemCommandError;
    String commandSystemUnknownError;
    String commandSystemConsoleOnlyError;
    String commandSystemPlayerOnlyError;
}
