package eu.okaeri.platform.velocity.i18n;

import eu.okaeri.configs.OkaeriConfig;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.kyori.adventure.text.format.TextColor;

import java.util.regex.Pattern;

@Getter
@Setter
public class I18nColorMatcher extends OkaeriConfig {

    static I18nColorMatcher of(@NonNull String pattern, @NonNull TextColor messageColor, @NonNull TextColor fieldsColor) {
        I18nColorMatcher matcher = new I18nColorMatcher();
        matcher.setPattern(Pattern.compile(pattern));
        matcher.setMessageColor(messageColor);
        matcher.setFieldsColor(fieldsColor);
        return matcher;
    }

    private Pattern pattern;
    private TextColor messageColor;
    private TextColor fieldsColor;
}
