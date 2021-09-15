package eu.okaeri.platform.core.loader;

import eu.okaeri.commons.cache.CacheMap;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.schema.ConfigDeclaration;
import eu.okaeri.configs.schema.FieldDeclaration;
import eu.okaeri.i18n.configs.LocaleConfig;
import eu.okaeri.injector.Injector;
import eu.okaeri.platform.core.component.ComponentCreator;
import eu.okaeri.platform.core.component.manifest.BeanManifest;
import lombok.*;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@AllArgsConstructor
@RequiredArgsConstructor
public class PlatformPreloader {

    private final Map<Class<?>, Boolean> isUnsafeAsyncCache = new CacheMap<>();
    @Getter private final List<AsyncLoader> preloaders = Collections.synchronizedList(new ArrayList<>());
    @Setter private Logger logger = Logger.getLogger(this.originator);

    @NonNull private String originator;
    private final boolean autoStart;
    @NonNull private final Set<String> asyncBannedTypes;

    public Thread createPreloadThread(@NonNull String name, @NonNull Runnable runnable) {
        Thread preloader = new Thread(runnable);
        preloader.setName("Okaeri Platform Preloader (" + this.originator + ") - " + name);
        return preloader;
    }

    public void preloadData(@NonNull String name, @NonNull Runnable runnable) {

        AsyncLoader asyncLoader = new AsyncLoader(name, runnable);
        Thread preloader = this.createPreloadThread(name, () -> {
            try {
                runnable.run();
                asyncLoader.setDone(true);
            } catch (Throwable exception) {
                this.logger.warning(name + ": " + exception.getMessage());
            }
        });

        asyncLoader.setThread(preloader);
        this.preloaders.add(asyncLoader);

        if (this.autoStart) {
            preloader.start();
        }
    }

    public void fallbackLoad() {
        this.preloaders.stream()
                .filter(loader -> !loader.getThread().isAlive())
                .filter(loader -> !loader.isDone())
                .map(loader -> {
                    this.logger.warning("- Fallback loading (async fail): " + loader.getName());
                    return loader.getRunnable();
                })
                .forEach(Runnable::run);
    }

    @SneakyThrows
    public void await() {
        for (Thread preloader : this.getPreloaders().stream().map(AsyncLoader::getThread).collect(Collectors.toList())) {
            if (this.autoStart) {
                preloader.join();
            } else {
                preloader.run();
            }
        }
    }

    public void fallbackLoadAndAwait() {
        this.fallbackLoad();
        this.await();
    }

    @SneakyThrows
    public void preloadConfig(@NonNull BeanManifest beanManifest, @NonNull Injector injector, @NonNull ComponentCreator creator) {
        List<BeanManifest> depends = beanManifest.getDepends();
        for (BeanManifest depend : depends) {
            if (!OkaeriConfig.class.isAssignableFrom(depend.getType()) // is not okaeri config
                    || LocaleConfig.class.isAssignableFrom(depend.getType()) // or is locale config
                    || !depend.ready(injector)) { // or is not ready (somehow has dependencies)
                continue;
            }
            if (this.isUnsafeAsync(depend.getType())) {
                continue;
            }
            depend.setObject(creator.makeObject(depend, injector));
            injector.registerInjectable(depend.getName(), depend.getObject());
        }
    }

    @SneakyThrows
    public void preloadLocaleConfig(@NonNull BeanManifest beanManifest, @NonNull Injector injector, @NonNull ComponentCreator creator) {
        List<BeanManifest> depends = beanManifest.getDepends();
        for (BeanManifest depend : depends) {
            if (!LocaleConfig.class.isAssignableFrom(depend.getType())  // is not locale config
                    || !depend.ready(injector)) { // or is not ready (somehow has dependencies)
                continue;
            }
            depend.setObject(creator.makeObject(depend, injector));
            injector.registerInjectable(depend.getName(), depend.getObject());
        }
    }

    public boolean isUnsafeAsync(@NonNull Class<?> configType) {

        Boolean cachedResult = this.isUnsafeAsyncCache.get(configType);
        if (cachedResult != null) {
            return cachedResult;
        }

        ConfigDeclaration declaration = ConfigDeclaration.of(configType);
        for (FieldDeclaration field : declaration.getFields()) {

            Class<?> fieldRealType = field.getType().getType();
            if (field.getType().isConfig()) {
                // subconfig - deep check
                if (!this.isUnsafeAsync(fieldRealType)) {
                    this.isUnsafeAsyncCache.put(configType, true);
                    return true;
                }
            }

            if (this.asyncBannedTypes.contains(fieldRealType.getCanonicalName())) {
                this.isUnsafeAsyncCache.put(configType, true);
                return true;
            }
        }

        this.isUnsafeAsyncCache.put(configType, false);
        return false;
    }
}
