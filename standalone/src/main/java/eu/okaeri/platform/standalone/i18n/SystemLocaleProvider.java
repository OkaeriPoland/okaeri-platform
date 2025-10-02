package eu.okaeri.platform.standalone.i18n;

import eu.okaeri.i18n.locale.LocaleProvider;
import lombok.NonNull;

import java.util.Locale;

public class SystemLocaleProvider implements LocaleProvider<Object> {

    private static final String FORCED_LOCALE = System.getProperty("okaeri.platform.forcedLocale");

    @Override
    public boolean supports(@NonNull Class type) {
        return true;
    }

    @Override
    public Locale getLocale(@NonNull Object entity) {

        if (FORCED_LOCALE != null) {
            return Locale.forLanguageTag(FORCED_LOCALE);
        }

        return Locale.getDefault();
    }
}
