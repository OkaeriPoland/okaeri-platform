package eu.okaeri.platform.core.plan.task;

import eu.okaeri.persistence.Persistence;
import eu.okaeri.platform.core.OkaeriPlatform;
import eu.okaeri.platform.core.component.ComponentHelper;
import eu.okaeri.platform.core.plan.ExecutionTask;

public class PersistenceShutdownTask implements ExecutionTask<OkaeriPlatform> {

    @Override
    public void execute(OkaeriPlatform platform) {
        ComponentHelper.closeAllOfType(Persistence.class, platform.getInjector());
    }
}
