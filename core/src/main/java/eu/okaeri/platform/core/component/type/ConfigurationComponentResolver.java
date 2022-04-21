package eu.okaeri.platform.core.component.type;

import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.configurer.Configurer;
import eu.okaeri.configs.serdes.OkaeriSerdesPack;
import eu.okaeri.configs.validator.okaeri.OkaeriValidator;
import eu.okaeri.injector.Injector;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.persistence.document.ConfigurerProvider;
import eu.okaeri.platform.core.annotation.Configuration;
import eu.okaeri.platform.core.annotation.Messages;
import eu.okaeri.platform.core.component.ComponentHelper;
import eu.okaeri.platform.core.component.creator.ComponentCreator;
import eu.okaeri.platform.core.component.creator.ComponentResolver;
import eu.okaeri.platform.core.component.manifest.BeanManifest;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Stream;

@NoArgsConstructor
@AllArgsConstructor
public class ConfigurationComponentResolver implements ComponentResolver {

    @Override
    public boolean supports(@NonNull Class<?> type) {
        return type.getAnnotation(Configuration.class) != null;
    }

    @Override
    public boolean supports(@NonNull Method method) {
        return false;
    }

    private @Inject ConfigurerProvider defaultConfigurerProvider;
    private @Inject Class<? extends OkaeriSerdesPack>[] defaultConfigurerSerdes;
    private @Inject File dataFolder;

    @Override
    @SuppressWarnings("unchecked")
    public Object make(@NonNull ComponentCreator creator, @NonNull BeanManifest manifest, @NonNull Injector injector) {

        if (!OkaeriConfig.class.isAssignableFrom(manifest.getType())) {
            throw new IllegalArgumentException("Component of @Configuration on type requires class to be a OkaeriConfig: " + manifest);
        }

        long start = System.currentTimeMillis();
        Class<? extends OkaeriConfig> configType = (Class<? extends OkaeriConfig>) manifest.getType();

        Messages messages = configType.getAnnotation(Messages.class);
        if (messages != null) {
            throw new IllegalArgumentException("Cannot register @Messages with raw OkaeriConfig type, use LocaleConfig: " + configType);
        }

        Configuration configuration = configType.getAnnotation(Configuration.class);
        String path = configuration.path();
        boolean defaultNotNull = configuration.defaultNotNull();
        Class<? extends Configurer> provider = configuration.provider();

        try {
            Configurer configurer = (provider == Configuration.DEFAULT.class)
                ? this.defaultConfigurerProvider.get()
                : injector.createInstance(provider);

            OkaeriSerdesPack[] serdesPacks = Stream.concat(Stream.of(this.defaultConfigurerSerdes), Arrays.stream(configuration.serdes()))
                .map(injector::createInstance)
                .distinct()
                .toArray(OkaeriSerdesPack[]::new);

            String extension = configurer.getExtensions().isEmpty() ? "bin" : configurer.getExtensions().get(0);
            String resolvedPath = path.replace("{ext}", extension);

            OkaeriConfig config = ConfigManager.create(configType, (it) -> {
                it.withBindFile(new File(this.dataFolder, resolvedPath));
                it.withConfigurer(new OkaeriValidator(configurer, defaultNotNull), serdesPacks);
                it.saveDefaults();
                it.load(true);
            });

            long took = System.currentTimeMillis() - start;
            creator.log(ComponentHelper.buildComponentMessage()
                .type("Loaded configuration")
                .name(configType.getSimpleName())
                .took(took)
                .meta("path", path)
                .meta("provider", provider.getSimpleName())
                .build());
            creator.increaseStatistics("configs", 1);

            return config;
        } catch (Exception exception) {
            throw new RuntimeException("Configuration load failure", exception);
        }
    }
}
