package eu.okaeri.platform.core.component.creator;

import eu.okaeri.injector.Injector;
import eu.okaeri.platform.core.component.ComponentCreator;
import eu.okaeri.platform.core.component.manifest.BeanManifest;
import lombok.NonNull;

import java.lang.reflect.Method;

public interface ComponentResolver {

    boolean supports(Class<?> type);

    boolean supports(Method method);

    Object make(@NonNull ComponentCreator creator, @NonNull BeanManifest manifest, @NonNull Injector injector);
}
