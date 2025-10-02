package eu.okaeri.platform.velocity.i18n;

import com.velocitypowered.api.proxy.Player;
import eu.okaeri.i18n.locale.LocaleProvider;
import lombok.*;

import java.util.Locale;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlayerLocaleProvider implements LocaleProvider<Player> {

    private static final String FORCED_LOCALE = System.getProperty("okaeri.platform.forcedLocale");
    private Locale fallbackLocale = null;

    @Override
    public boolean supports(@NonNull Class<?> type) {
        return Player.class.isAssignableFrom(type);
    }

    @Override
    public Locale getLocale(@NonNull Player player) {

        if (FORCED_LOCALE != null) {
            return Locale.forLanguageTag(FORCED_LOCALE);
        }

        return player.getEffectiveLocale() == null ? this.fallbackLocale : player.getEffectiveLocale();
    }
}
