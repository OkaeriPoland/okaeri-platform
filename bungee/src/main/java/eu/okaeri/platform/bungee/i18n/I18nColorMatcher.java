package eu.okaeri.platform.bungee.i18n;

import eu.okaeri.configs.OkaeriConfig;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;

import java.util.regex.Pattern;

@Getter
@Setter
public class I18nColorMatcher extends OkaeriConfig {

    static I18nColorMatcher of(@NonNull String pattern, @NonNull ChatColor messageColor, @NonNull ChatColor fieldsColor) {
        I18nColorMatcher matcher = new I18nColorMatcher();
        matcher.setPattern(Pattern.compile(pattern));
        matcher.setMessageColor(messageColor);
        matcher.setFieldsColor(fieldsColor);
        return matcher;
    }

    private Pattern pattern;
    private ChatColor messageColor;
    private ChatColor fieldsColor;
}
