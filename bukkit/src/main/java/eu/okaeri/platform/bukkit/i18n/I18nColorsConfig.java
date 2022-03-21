package eu.okaeri.platform.bukkit.i18n;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Header;
import lombok.Getter;
import org.bukkit.ChatColor;

import java.util.Arrays;
import java.util.List;

@Getter
@Header("This file allows to define coloring pattern")
@Header("for all the messages that do not have defined colors.")
@Header(" ")
@Header("Rules are checked from the top to the bottom.")
@Header("First rule matching key name would be applied.")
@Header(" ")
@Header("Rule defines regex pattern, color for the message")
@Header("itself and the color for dynamic fields e.g. {xyz}.")
@Header(" ")
@Header("Available colors:")
@Header("&4 - DARK_RED")
@Header("&c - RED")
@Header("&6 - GOLD")
@Header("&e - YELLOW")
@Header("&2 - DARK_GREEN")
@Header("&a - GREEN")
@Header("&b - AQUA")
@Header("&3 - DARK_AQUA")
@Header("&1 - DARK_BLUE")
@Header("&9 - BLUE")
@Header("&d - LIGHT_PURPLE")
@Header("&5 - DARK_PURPLE")
@Header("&f - WHITE")
@Header("&8 - DARK_GRAY")
@Header("&0 - BLACK")
public class I18nColorsConfig extends OkaeriConfig {

    private List<I18nColorMatcher> matchers = Arrays.asList(
        I18nColorMatcher.of("^.*(fail|rejected|not.?found|limit.?reached|already|cannot|invalid|error|alert|warning).*$", ChatColor.RED, ChatColor.YELLOW),
        I18nColorMatcher.of("^.*(success|succeeded|accepted|created|removed|added|done|ended).*$", ChatColor.GREEN, ChatColor.WHITE),
        I18nColorMatcher.of("^.*$", ChatColor.YELLOW, ChatColor.WHITE)
    );
}
