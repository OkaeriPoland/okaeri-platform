package eu.okaeri.platform.standalone.component;

import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.platform.core.OkaeriPlatform;
import eu.okaeri.platform.core.component.creator.ComponentCreator;
import eu.okaeri.platform.core.component.creator.ComponentCreatorRegistry;
import lombok.NonNull;

public class ApplicationComponentCreator extends ComponentCreator {

    private final OkaeriPlatform app;

    @Inject
    public ApplicationComponentCreator(@NonNull OkaeriPlatform app, @NonNull ComponentCreatorRegistry creatorRegistry) {
        super(creatorRegistry);
        this.app = app;
    }

    @Override
    public boolean isComponent(@NonNull Class<?> type) {
        return OkaeriPlatform.class.isAssignableFrom(type) || super.isComponent(type);
    }

    @Override
    public void log(@NonNull String message) {
        this.app.log(message);
    }
}
