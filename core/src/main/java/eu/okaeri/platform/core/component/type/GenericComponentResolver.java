package eu.okaeri.platform.core.component.type;

import eu.okaeri.injector.Injector;
import eu.okaeri.platform.core.annotation.Component;
import eu.okaeri.platform.core.component.ComponentHelper;
import eu.okaeri.platform.core.component.creator.ComponentCreator;
import eu.okaeri.platform.core.component.creator.ComponentResolver;
import eu.okaeri.platform.core.component.manifest.BeanManifest;
import lombok.NonNull;

import java.lang.reflect.Method;

/**
 * Resolves generic @Component classes.
 * <p>
 * Remember to register last as otherwise {@link #supports(Class)} may interfere
 * with other ComponentResolver implementations that due to being 3rd party
 * extension are not provided with custom annotation e.g. bukkit's listeners.
 */
public class GenericComponentResolver implements ComponentResolver {

    @Override
    public boolean supports(@NonNull Class<?> type) {
        return type.getAnnotation(Component.class) != null;
    }

    @Override
    public boolean supports(@NonNull Method method) {
        return false;
    }

    @Override
    public Object make(@NonNull ComponentCreator creator, @NonNull BeanManifest manifest, @NonNull Injector injector) {

        long start = System.currentTimeMillis();
        Object result = injector.createInstance(manifest.getType());

        long took = System.currentTimeMillis() - start;
        if (took > 1) {
            creator.log(ComponentHelper.buildComponentMessage()
                .type("Added generic component")
                .name(manifest.getType().getSimpleName())
                .took(took)
                .build());
        }
        creator.increaseStatistics("components", 1);

        return result;
    }
}
