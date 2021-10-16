package eu.okaeri.platform.standalone.i18n;

import eu.okaeri.i18n.provider.LocaleProvider;

import java.util.Locale;

public class SystemLocaleProvider implements LocaleProvider<Object> {

    @Override
    public boolean supports(Class type) {
        return true;
    }

    @Override
    public Locale getLocale(Object entity) {
        return Locale.getDefault();
    }
}
