package eu.okaeri.platform.core.plan.task;

import eu.okaeri.injector.OkaeriInjector;
import eu.okaeri.platform.core.OkaeriPlatform;
import eu.okaeri.platform.core.plan.ExecutionTask;

public class InjectorSetupTask implements ExecutionTask<OkaeriPlatform> {

    @Override
    public void execute(OkaeriPlatform platform) {
        OkaeriInjector injector = OkaeriInjector.create(true);
        platform.setInjector(injector);
        platform.registerInjectable("injector", injector);
    }
}
