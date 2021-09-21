package eu.okaeri.platform.bungee.i18n;

import eu.okaeri.i18n.provider.LocaleProvider;
import lombok.NonNull;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Locale;

public class ProxiedPlayerLocaleProvider implements LocaleProvider<ProxiedPlayer> {

    @Override
    public boolean supports(@NonNull Class<?> type) {
        return ProxiedPlayer.class.isAssignableFrom(type);
    }

    @Override
    public Locale getLocale(@NonNull ProxiedPlayer player) {
        return player.getLocale();
    }
}
