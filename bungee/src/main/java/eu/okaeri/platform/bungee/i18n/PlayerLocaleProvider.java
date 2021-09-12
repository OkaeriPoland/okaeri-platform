package eu.okaeri.platform.bungee.i18n;

import eu.okaeri.i18n.provider.LocaleProvider;
import lombok.NonNull;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Locale;

public class PlayerLocaleProvider implements LocaleProvider<ProxiedPlayer> {

    @Override
    public boolean supports(@NonNull Class<?> type) {
        return ProxiedPlayer.class.isAssignableFrom(type);
    }

    @Override
    public Locale getLocale(@NonNull ProxiedPlayer player) {

//        String localeString = UnsafeBukkitCommons.getLocaleString(player);
//        localeString = localeString.replace("_", "-");
//
//        return Locale.forLanguageTag(localeString);
        return Locale.forLanguageTag("pl-PL"); // TODO: fix locale
    }
}
