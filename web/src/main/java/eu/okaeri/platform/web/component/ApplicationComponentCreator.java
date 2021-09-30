package eu.okaeri.platform.web.component;

import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.platform.core.component.creator.ComponentCreator;
import eu.okaeri.platform.core.component.creator.ComponentCreatorRegistry;
import eu.okaeri.platform.web.OkaeriWebApplication;
import lombok.NonNull;

public class ApplicationComponentCreator extends ComponentCreator {

    private final OkaeriWebApplication app;

    @Inject
    public ApplicationComponentCreator(@NonNull OkaeriWebApplication app, @NonNull ComponentCreatorRegistry creatorRegistry) {
        super(creatorRegistry);
        this.app = app;
    }

    @Override
    public boolean isComponent(@NonNull Class<?> type) {
        return OkaeriWebApplication.class.isAssignableFrom(type) || super.isComponent(type);
    }

    @Override
    public void log(@NonNull String message) {
        this.app.getLogger().info(message);
    }
}
