package eu.okaeri.platform.core.component.type;

import eu.okaeri.injector.Injector;
import eu.okaeri.platform.core.annotation.Bean;
import eu.okaeri.platform.core.component.ComponentCreator;
import eu.okaeri.platform.core.component.ComponentHelper;
import eu.okaeri.platform.core.component.creator.ComponentResolver;
import eu.okaeri.platform.core.component.manifest.BeanManifest;
import lombok.NonNull;

import java.lang.reflect.Method;

public class BeanComponentResolver implements ComponentResolver {

    @Override
    public boolean supports(Class<?> type) {
        return false;
    }

    @Override
    public boolean supports(Method method) {
        return method.getAnnotation(Bean.class) != null;
    }

    @Override
    public Object make(@NonNull ComponentCreator creator, @NonNull BeanManifest manifest, @NonNull Injector injector) {
        return ComponentHelper.invokeMethod(manifest, injector);
    }
}
