package eu.okaeri.platform.core.plan.task;

import eu.okaeri.platform.core.OkaeriPlatform;
import eu.okaeri.platform.core.component.ComponentHelper;
import eu.okaeri.platform.core.plan.ExecutionTask;
import lombok.RequiredArgsConstructor;

import java.io.Closeable;

@RequiredArgsConstructor
public class CloseableShutdownTask implements ExecutionTask<OkaeriPlatform> {

    private final Class<? extends Closeable> type;

    @Override
    public void execute(OkaeriPlatform platform) {
        ComponentHelper.closeAllOfType(this.type, platform.getInjector());
    }
}
