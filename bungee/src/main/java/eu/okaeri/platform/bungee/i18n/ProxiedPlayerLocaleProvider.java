package eu.okaeri.platform.bungee.i18n;

import eu.okaeri.i18n.provider.LocaleProvider;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Locale;

@NoArgsConstructor
@AllArgsConstructor
public class ProxiedPlayerLocaleProvider implements LocaleProvider<ProxiedPlayer> {

    private Locale fallbackLocale = null;

    @Override
    public boolean supports(@NonNull Class<?> type) {
        return ProxiedPlayer.class.isAssignableFrom(type);
    }

    @Override
    public Locale getLocale(@NonNull ProxiedPlayer player) {
        return player.getLocale() == null ? this.fallbackLocale : player.getLocale();
    }
}
