package eu.okaeri.platform.cli.component;

import eu.okaeri.platform.cli.OkaeriCliApplication;
import eu.okaeri.platform.core.component.creator.ComponentCreator;
import eu.okaeri.platform.core.component.creator.ComponentCreatorRegistry;
import lombok.NonNull;

public class ApplicationComponentCreator extends ComponentCreator {

    private static final boolean DEBUG = Boolean.parseBoolean(System.getProperty("okaeri.platform.debug", "false"));

    private final OkaeriCliApplication app;

    public ApplicationComponentCreator(@NonNull OkaeriCliApplication app, @NonNull ComponentCreatorRegistry creatorRegistry) {
        super(creatorRegistry);
        this.app = app;
    }

    @Override
    public boolean isComponent(@NonNull Class<?> type) {
        return OkaeriCliApplication.class.isAssignableFrom(type) || super.isComponent(type);
    }

    @Override
    public void log(@NonNull String message) {
        if (DEBUG) {
            this.app.getLogger().info(message);
        }
    }
}
