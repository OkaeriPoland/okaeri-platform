package eu.okaeri.platform.bukkit.commons.i18n;

import eu.okaeri.configs.OkaeriConfig;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;

import java.util.regex.Pattern;

@Getter
@Setter
public class I18nColorMatcher extends OkaeriConfig {

    static I18nColorMatcher of(String pattern, ChatColor messageColor, ChatColor fieldsColor) {
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
