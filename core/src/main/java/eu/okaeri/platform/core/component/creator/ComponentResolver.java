package eu.okaeri.platform.core.component.creator;

import eu.okaeri.injector.Injector;
import eu.okaeri.platform.core.component.manifest.BeanManifest;
import lombok.NonNull;

import java.lang.reflect.Method;

public interface ComponentResolver {

    boolean supports(@NonNull Class<?> type);

    boolean supports(@NonNull Method method);

    Object make(@NonNull ComponentCreator creator, @NonNull BeanManifest manifest, @NonNull Injector injector);
}
