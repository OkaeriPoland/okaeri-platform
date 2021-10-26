package eu.okaeri.platform.core.plan.task;

import eu.okaeri.platform.core.OkaeriPlatform;
import eu.okaeri.platform.core.component.manifest.BeanManifest;
import eu.okaeri.platform.core.plan.ExecutionTask;

public class BeanManifestCreateTask implements ExecutionTask<OkaeriPlatform> {

    @Override
    public void execute(OkaeriPlatform platform) {

        // create manifest of the platform
        ClassLoader classLoader = platform.getClass().getClassLoader();
        BeanManifest beanManifest = BeanManifest.of(classLoader, platform.getClass(), platform.getCreator(), true);
        beanManifest.setObject(platform);

        // register injectable
        platform.registerInjectable("manifest", beanManifest);
    }
}
