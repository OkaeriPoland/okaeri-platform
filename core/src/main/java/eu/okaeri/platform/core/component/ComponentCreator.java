package eu.okaeri.platform.core.component;

import eu.okaeri.injector.Injector;
import eu.okaeri.platform.core.component.manifest.BeanManifest;

public interface ComponentCreator {

    Object makeObject(BeanManifest beanManifest, Injector injector);

    boolean isComponent(Class<?> type);
}
