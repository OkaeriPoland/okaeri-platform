package eu.okaeri.platform.bungee.i18n;

import eu.okaeri.i18n.locale.LocaleProvider;
import lombok.*;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Locale;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProxiedPlayerLocaleProvider implements LocaleProvider<ProxiedPlayer> {

    private static final String FORCED_LOCALE = System.getProperty("okaeri.platform.forcedLocale");
    private Locale fallbackLocale = null;

    @Override
    public boolean supports(@NonNull Class<?> type) {
        return ProxiedPlayer.class.isAssignableFrom(type);
    }

    @Override
    public Locale getLocale(@NonNull ProxiedPlayer player) {

        if (FORCED_LOCALE != null) {
            return Locale.forLanguageTag(FORCED_LOCALE);
        }

        return player.getLocale() == null ? this.fallbackLocale : player.getLocale();
    }
}
