package eu.okaeri.platform.standalone.component.type;

import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.configurer.Configurer;
import eu.okaeri.configs.serdes.OkaeriSerdesPack;
import eu.okaeri.i18n.configs.LocaleConfig;
import eu.okaeri.i18n.configs.LocaleConfigManager;
import eu.okaeri.i18n.configs.simple.MessageOCI18n;
import eu.okaeri.i18n.locale.LocaleProvider;
import eu.okaeri.injector.Injector;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.persistence.document.ConfigurerProvider;
import eu.okaeri.placeholders.Placeholders;
import eu.okaeri.platform.core.annotation.Messages;
import eu.okaeri.platform.core.component.ComponentHelper;
import eu.okaeri.platform.core.component.creator.ComponentCreator;
import eu.okaeri.platform.core.component.creator.ComponentResolver;
import eu.okaeri.platform.core.component.manifest.BeanManifest;
import eu.okaeri.platform.core.placeholder.PlaceholdersFactory;
import lombok.NonNull;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MessagesComponentResolver implements ComponentResolver {

    @Override
    public boolean supports(@NonNull Class<?> type) {
        return type.getAnnotation(Messages.class) != null;
    }

    @Override
    public boolean supports(@NonNull Method method) {
        return false;
    }

    private @Inject PlaceholdersFactory defaultPlaceholdersFactory; // TODO: make use of this
    private @Inject ConfigurerProvider defaultConfigurerProvider;
    private @Inject Class<? extends OkaeriSerdesPack>[] defaultConfigurerSerdes;
    private @Inject LocaleProvider<?> i18nLocaleProvider;

    private @Inject("dataFolder") File dataFolder;
    private @Inject("jarFile") File jarFile;

    @Override
    @SuppressWarnings({"unchecked", "ResultOfMethodCallIgnored"})
    public Object make(@NonNull ComponentCreator creator, @NonNull BeanManifest manifest, @NonNull Injector injector) {

        if (!LocaleConfig.class.isAssignableFrom(manifest.getType())) {
            throw new IllegalArgumentException("Component of @Messages on type requires class to be a LocaleConfig: " + manifest);
        }

        Placeholders defaultPlaceholders = injector.getExact("placeholders", Placeholders.class)
            .orElseThrow(() -> new IllegalArgumentException("cannot find placeholders required for @Messages"));

        long start = System.currentTimeMillis();
        Class<? extends LocaleConfig> beanClazz = (Class<? extends LocaleConfig>) manifest.getType();
        Messages messages = beanClazz.getAnnotation(Messages.class);

        String path = messages.path();
        String suffix = messages.suffix();
        Class<? extends Configurer> provider = messages.provider();
        Locale defaultLocale = Locale.forLanguageTag(messages.defaultLocale());
        boolean unpack = messages.unpack();
        File directory = new File(this.dataFolder, path);
        boolean directoryExisted = directory.exists();
        Map<Locale, String> packedLocales = new LinkedHashMap<>();

        if (unpack) {
            directory.mkdirs();
        }

        // unpack files from the resources
        try {
            JarFile jarFile = new JarFile(this.jarFile);
            Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()) {

                JarEntry jarEntry = entries.nextElement();
                String entryName = jarEntry.getName();

                if (!entryName.startsWith(path + "/") || entryName.endsWith("/")) {
                    continue;
                }

                File file = new File(this.dataFolder, entryName);
                if (file.exists()) {
                    continue;
                }

                InputStream is = jarFile.getInputStream(jarEntry);
                FileOutputStream fos = (unpack && !directoryExisted) ? new FileOutputStream(file) : null;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                while (is.available() > 0) {
                    int read = is.read();
                    if (fos != null) fos.write(read);
                    baos.write(read);
                }
                if (fos != null) fos.close();
                is.close();

                String name = file.getName();
                String localeName = name.substring(0, name.length() - suffix.length());

                Locale locale = Locale.forLanguageTag(localeName);
                packedLocales.put(locale, new String(baos.toByteArray(), StandardCharsets.UTF_8));
            }
        } catch (IOException exception) {
            throw new RuntimeException("Failed to unpack resources", exception);
        }

        // prepare serdes
        OkaeriSerdesPack[] serdesPacks = Stream.of(this.defaultConfigurerSerdes)
            .map(injector::createInstance)
            .distinct()
            .toArray(OkaeriSerdesPack[]::new);

        // load file locales
        try {
            LocaleConfig template = LocaleConfigManager.createTemplate(beanClazz);
            File[] files = directory.listFiles((dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(suffix));
            if (files == null) files = new File[0];

            MessageOCI18n i18n = new MessageOCI18n();
            i18n.setDefaultLocale(defaultLocale);
            i18n.registerLocaleProvider(this.i18nLocaleProvider);
            i18n.setPlaceholders(defaultPlaceholders.copy());

            List<Locale> loadedLocales = new ArrayList<>();
            injector.registerInjectable(path, template);

            // check path directory for locale files
            for (File file : files) {
                // read locale from name
                String name = file.getName();
                String localeName = name.substring(0, name.length() - suffix.length());
                Locale locale = Locale.forLanguageTag(localeName);
                // create configurer
                Configurer configurer = (provider == Messages.DEFAULT.class)
                    ? this.defaultConfigurerProvider.get()
                    : injector.createInstance(provider);
                // register
                LocaleConfig localeConfig = LocaleConfigManager.create(beanClazz, configurer, file, !defaultLocale.equals(locale));
                i18n.registerConfig(locale, localeConfig);
                creator.increaseStatistics("localeConfigs", 1);
                loadedLocales.add(locale);
            }

            // load packes locales
            for (Map.Entry<Locale, String> entry : packedLocales.entrySet()) {
                // gather data
                Locale locale = entry.getKey();
                String configString = entry.getValue();
                // already loaded from file
                if (loadedLocales.contains(locale)) continue;
                // create configurer
                Configurer configurer = (provider == Messages.DEFAULT.class)
                    ? this.defaultConfigurerProvider.get()
                    : injector.createInstance(provider);
                // register
                LocaleConfig localeConfig = ConfigManager.create(beanClazz, (it) -> {
                    it.withConfigurer(configurer);
                    if (!defaultLocale.equals(locale)) it.getDeclaration().getFields().forEach((field) -> field.updateValue(null));
                    it.load(configString);
                });
                i18n.registerConfig(locale, localeConfig);
                creator.increaseStatistics("localeConfigs", 1);
                loadedLocales.add(locale);
            }

            // default locale was not overwritten by a file
            if (!loadedLocales.contains(defaultLocale)) {
                // create configurer
                Configurer configurer = (provider == Messages.DEFAULT.class)
                    ? this.defaultConfigurerProvider.get()
                    : injector.createInstance(provider);
                // register default locale based on interface values
                LocaleConfig defaultLocaleConfig = ConfigManager.create(beanClazz, it -> {
                    it.withBindFile(new File(directory, messages.defaultLocale() + suffix));
                    it.withConfigurer(configurer);
                    if (unpack && !directoryExisted) it.saveDefaults();
                });
                i18n.registerConfig(defaultLocale, defaultLocaleConfig);
                creator.increaseStatistics("localeConfigs", 1);
                loadedLocales.add(defaultLocale);
            }

            long took = System.currentTimeMillis() - start;
            creator.log(ComponentHelper.buildComponentMessage()
                .type("Loaded messages")
                .name(beanClazz.getSimpleName())
                .took(took)
                .meta("path", path)
                .meta("suffix", suffix)
                .meta("provider", provider.getSimpleName())
                .footer("  > " + loadedLocales.stream().map(Locale::toString).collect(Collectors.joining(", ")))
                .build());

            manifest.setName(path);
            return i18n;
        } catch (Exception exception) {
            throw new RuntimeException("Messages configuration load failure", exception);
        }
    }
}
