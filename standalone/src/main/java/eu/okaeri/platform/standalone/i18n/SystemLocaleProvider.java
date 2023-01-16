package eu.okaeri.platform.standalone.i18n;

import eu.okaeri.i18n.locale.LocaleProvider;
import lombok.NonNull;

import java.util.Locale;

public class SystemLocaleProvider implements LocaleProvider<Object> {

    @Override
    public boolean supports(@NonNull Class type) {
        return true;
    }

    @Override
    public Locale getLocale(@NonNull Object entity) {
        return Locale.getDefault();
    }
}
