package eu.okaeri.platform.core;

import eu.okaeri.injector.Injector;
import eu.okaeri.platform.core.component.creator.ComponentCreator;
import eu.okaeri.platform.core.plan.ExecutionPlan;
import lombok.NonNull;

public interface OkaeriPlatform {

    // injector
    void setInjector(@NonNull Injector injector);

    Injector getInjector();

    default void registerInjectable(@NonNull String name, @NonNull Object type) {
        this.getInjector().registerInjectable(name, type);
    }

    default <T> T createInstance(@NonNull Class<T> type) {
        return this.getInjector().createInstance(type);
    }

    // creator
    void setCreator(@NonNull ComponentCreator creator);

    ComponentCreator getCreator();

    // logging
    void log(@NonNull String message);

    // setup
    void plan(@NonNull ExecutionPlan plan);
}
