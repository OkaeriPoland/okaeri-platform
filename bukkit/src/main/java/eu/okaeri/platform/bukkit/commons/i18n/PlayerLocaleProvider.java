package eu.okaeri.platform.bukkit.commons.i18n;

import eu.okaeri.i18n.provider.LocaleProvider;
import org.bukkit.entity.Player;

import java.util.Locale;

public class PlayerLocaleProvider implements LocaleProvider<Player> {

    @Override
    public boolean supports(Class<?> type) {
        return Player.class.isAssignableFrom(type);
    }

    @Override
    public Locale getLocale(Player player) {
        return Locale.forLanguageTag(player.getLocale().replace("_", "-"));
    }
}
