package eu.okaeri.platform.velocity.i18n;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Header;
import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;

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
@Header("- black, dark_blue, dark_green, dark_aqua, dark_red, dark_purple, gold, gray, dark_gray, blue, green, aqua, red, light_purple, yellow, or white")
@Header("- any hex color in CSS format")
public class I18nColorsConfig extends OkaeriConfig {

    private List<I18nColorMatcher> matchers = Arrays.asList(
        I18nColorMatcher.of("^.*(fail|rejected|not.?found|limit.?reached|already|cannot|invalid|error|alert|warning).*$", NamedTextColor.RED, NamedTextColor.YELLOW),
        I18nColorMatcher.of("^.*(success|succeeded|accepted|created|removed|added|done|ended).*$", NamedTextColor.GREEN, NamedTextColor.WHITE),
        I18nColorMatcher.of("^.*$", NamedTextColor.YELLOW, NamedTextColor.WHITE)
    );
}
