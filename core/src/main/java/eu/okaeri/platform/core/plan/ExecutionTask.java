package eu.okaeri.platform.core.plan;

import eu.okaeri.platform.core.OkaeriPlatform;

@FunctionalInterface
public interface ExecutionTask<T extends OkaeriPlatform> {
    void execute(T platform);
}
