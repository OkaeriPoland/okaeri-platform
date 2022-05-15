package eu.okaeri.platform.bukkit.i18n;

import eu.okaeri.commons.bukkit.UnsafeBukkitCommons;
import eu.okaeri.i18n.provider.LocaleProvider;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.entity.Player;

import java.util.Locale;

@NoArgsConstructor
@AllArgsConstructor
public class PlayerLocaleProvider implements LocaleProvider<Player> {

    private Locale fallbackLocale = null;

    @Override
    public boolean supports(@NonNull Class<?> type) {
        return Player.class.isAssignableFrom(type);
    }

    @Override
    public Locale getLocale(@NonNull Player player) {

        String localeString = UnsafeBukkitCommons.getLocaleString(player);
        if (localeString == null) {
            return this.fallbackLocale;
        }

        localeString = localeString.replace("_", "-");
        return Locale.forLanguageTag(localeString);
    }
}
