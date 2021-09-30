package eu.okaeri.platform.core.plan.task;

import eu.okaeri.platform.core.OkaeriPlatform;
import eu.okaeri.platform.core.component.creator.ComponentCreator;
import eu.okaeri.platform.core.component.creator.ComponentCreatorRegistry;
import eu.okaeri.platform.core.plan.ExecutionTask;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreatorSetupTask implements ExecutionTask<OkaeriPlatform> {

    private final Class<? extends ComponentCreator> creatorType;
    private final Class<? extends ComponentCreatorRegistry> registryType;

    @Override
    public void execute(OkaeriPlatform platform) {

        ComponentCreatorRegistry registry = platform.createInstance(this.registryType);
        platform.registerInjectable("creatorRegistry", registry);

        ComponentCreator componentCreator = platform.createInstance(this.creatorType);
        platform.registerInjectable("creator", componentCreator);

        platform.setCreator(componentCreator);
    }
}
