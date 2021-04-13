package eu.okaeri.platform.bukkit.commons.i18n;

import eu.okaeri.i18n.provider.LocaleProvider;
import eu.okaeri.platform.bukkit.commons.UnsafeBukkitCommons;
import org.bukkit.entity.Player;

import java.util.Locale;

public class PlayerLocaleProvider implements LocaleProvider<Player> {

    @Override
    public boolean supports(Class<?> type) {
        return Player.class.isAssignableFrom(type);
    }

    @Override
    public Locale getLocale(Player player) {

        String localeString = UnsafeBukkitCommons.getLocaleString(player);
        localeString = localeString.replace("_", "-");

        return Locale.forLanguageTag(localeString);
    }
}
