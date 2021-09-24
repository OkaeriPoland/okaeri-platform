package eu.okaeri.platform.core.loader;

import eu.okaeri.commands.service.CommandService;
import eu.okaeri.commons.cache.CacheMap;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.schema.ConfigDeclaration;
import eu.okaeri.configs.schema.FieldDeclaration;
import eu.okaeri.i18n.configs.LocaleConfig;
import eu.okaeri.injector.Injector;
import eu.okaeri.platform.core.component.creator.ComponentCreator;
import eu.okaeri.platform.core.component.manifest.BeanManifest;
import eu.okaeri.platform.core.component.manifest.BeanSource;
import lombok.*;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@AllArgsConstructor
@RequiredArgsConstructor
public class PlatformPreloader {

    private final Map<Class<?>, Boolean> isUnsafeAsyncCache = new CacheMap<>();
    @Getter private final List<AsyncLoader> preloaders = Collections.synchronizedList(new ArrayList<>());
    @Setter private Logger logger = Logger.getGlobal();

    private final String originator;
    private final boolean autoStart;
    @NonNull private final Set<String> asyncBannedTypes;

    public boolean hasLoaded(String... preloaders) {
        Set<String> names = new HashSet<>(Arrays.asList(preloaders));
        return this.preloaders.stream()
                .filter(preloader -> names.contains(preloader.getName()))
                .filter(AsyncLoader::isDone)
                .count() == names.size();
    }

    @SneakyThrows
    @SuppressWarnings("BusyWait")
    public void await(String... preloaders) {
        while (!this.hasLoaded(preloaders)) {
            Thread.sleep(1);
        }
    }

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
                preloader.start();
            }
        }
    }

    public void fallbackLoadAndAwait() {
        this.fallbackLoad();
        this.await();
    }

    public void preloadBeans(@NonNull BeanManifest beanManifest, @NonNull Injector injector, @NonNull ComponentCreator creator) {
        List<BeanManifest> depends = beanManifest.getDepends();
        for (BeanManifest depend : depends) {
            // only targeting @Bean
            if (depend.getSource() != BeanSource.METHOD) {
                continue;
            }
            // must be marked as preload
            if (!depend.isPreload()) {
                continue;
            }
            // start preload thread
            this.preloadData("Bean - " + depend.getName(), () -> this.awaitDepend(depend, injector, creator));
        }
    }

    @SneakyThrows
    @SuppressWarnings("BusyWait")
    public void awaitDepend(BeanManifest depend, Injector injector, ComponentCreator creator) {
        // await when ready
        while (!depend.ready(creator, injector, false)) {
            Thread.sleep(1);
        }
        // update dependencies
        depend.update(creator, injector);
        // everything ok, make
        depend.setObject(creator.make(depend, injector));
        injector.registerInjectable(depend.getName(), depend.getObject());
    }

    public void preloadCommands(@NonNull BeanManifest beanManifest, @NonNull Injector injector, @NonNull ComponentCreator creator) {
        List<BeanManifest> depends = beanManifest.getDepends();
        for (BeanManifest depend : depends) {
            // only targeting @Register
            if (depend.getSource() != BeanSource.COMPONENT) {
                continue;
            }
            // must be a CommandService and ready
            if (!CommandService.class.isAssignableFrom(depend.getType()) || !depend.ready(creator, injector)) {
                continue;
            }
            // everything ok, preload
            depend.setObject(creator.make(depend, injector));
            injector.registerInjectable(depend.getName(), depend.getObject());
        }
    }

    public void preloadConfig(@NonNull BeanManifest beanManifest, @NonNull Injector injector, @NonNull ComponentCreator creator) {
        List<BeanManifest> depends = beanManifest.getDepends();
        for (BeanManifest depend : depends) {
            // component only, method beans would not work
            // due to missing parent etc
            if (depend.getSource() != BeanSource.COMPONENT) {
                continue;
            }
            // basic type blacklist
            if (!OkaeriConfig.class.isAssignableFrom(depend.getType()) // is not okaeri config
                    || LocaleConfig.class.isAssignableFrom(depend.getType()) // or is locale config
                    || !depend.ready(creator, injector)) { // or is not ready (somehow has dependencies)
                continue;
            }
            // async safety check
            if (this.isUnsafeAsync(depend.getType())) {
                continue;
            }
            // everything ok, preload
            depend.setObject(creator.make(depend, injector));
            injector.registerInjectable(depend.getName(), depend.getObject());
        }
    }

    public void preloadLocaleConfig(@NonNull BeanManifest beanManifest, @NonNull Injector injector, @NonNull ComponentCreator creator) {
        List<BeanManifest> depends = beanManifest.getDepends();
        for (BeanManifest depend : depends) {
            // component only, method beans would not work
            // due to missing parent etc
            if (depend.getSource() != BeanSource.COMPONENT) {
                continue;
            }
            // basic type blacklist
            if (!LocaleConfig.class.isAssignableFrom(depend.getType())  // is not locale config
                    || !depend.ready(creator, injector)) { // or is not ready (somehow has dependencies)
                continue;
            }
            // everything ok, preload
            depend.setObject(creator.make(depend, injector));
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
                if (this.isUnsafeAsync(fieldRealType)) {
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
