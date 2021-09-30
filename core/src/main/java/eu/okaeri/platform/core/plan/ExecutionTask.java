package eu.okaeri.platform.core.plan;

import eu.okaeri.platform.core.OkaeriPlatform;

public interface ExecutionTask<T extends OkaeriPlatform> {
    void execute(T platform);
}
