package eu.okaeri.platform.core.component.type;

import eu.okaeri.injector.Injector;
import eu.okaeri.platform.core.annotation.Component;
import eu.okaeri.platform.core.component.creator.ComponentCreator;
import eu.okaeri.platform.core.component.creator.ComponentResolver;
import eu.okaeri.platform.core.component.manifest.BeanManifest;
import lombok.NonNull;

import java.lang.reflect.Method;

/**
 * Resolves generic @Component classes.
 *
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
        return injector.createInstance(manifest.getType());
    }
}
