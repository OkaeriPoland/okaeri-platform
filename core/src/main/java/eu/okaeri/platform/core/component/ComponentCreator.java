package eu.okaeri.platform.core.component;

import eu.okaeri.injector.Injector;
import eu.okaeri.platform.core.component.manifest.BeanManifest;

import java.lang.reflect.Method;

public interface ComponentCreator {

    Object make(BeanManifest beanManifest, Injector injector);

    boolean isComponent(Class<?> type);

    boolean isComponentMethod(Method method);

    void increaseStatistics(String identifier, int count);

    void log(String message);
}
