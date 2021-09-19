package eu.okaeri.platform.bungee.component.type;

import eu.okaeri.injector.Injector;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.platform.bungee.annotation.Delayed;
import eu.okaeri.platform.core.component.ComponentCreator;
import eu.okaeri.platform.core.component.creator.ComponentResolver;
import eu.okaeri.platform.core.component.manifest.BeanManifest;
import eu.okaeri.platform.core.component.manifest.BeanSource;
import lombok.NonNull;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.TaskScheduler;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import static eu.okaeri.platform.core.component.ComponentHelper.invokeMethod;

public class DelayedComponentResolver implements ComponentResolver {

    @Override
    public boolean supports(Class<?> type) {
        return type.getAnnotation(Delayed.class) != null;
    }

    @Override
    public boolean supports(Method method) {
        return method.getAnnotation(Delayed.class) != null;
    }

    @Inject private Plugin plugin;
    @Inject private TaskScheduler scheduler;

    @Override
    public Object make(@NonNull ComponentCreator creator, @NonNull BeanManifest manifest, @NonNull Injector injector) {

        Object object;
        if (manifest.getSource() == BeanSource.METHOD) {
            object = (Runnable) () -> invokeMethod(manifest, injector);
            manifest.setName(manifest.getMethod().getName());
        } else {
            if (!Runnable.class.isAssignableFrom(manifest.getType())) {
                throw new IllegalArgumentException("Component of @Delayed on type requires class to be a java.lang.Runnable: " + manifest);
            }
            object = injector.createInstance(manifest.getType());
        }

        Runnable runnable = (Runnable) object;
        Delayed delayed = manifest.getSource() == BeanSource.METHOD
                ? manifest.getMethod().getAnnotation(Delayed.class)
                : manifest.getType().getAnnotation(Delayed.class);

        if (!delayed.name().isEmpty()) {
            manifest.setName(delayed.name());
        }

        int delay = delayed.time();
        TimeUnit timeUnit = delayed.timeUnit();

        this.scheduler.schedule(this.plugin, runnable, delay, timeUnit);

        String scheduledMeta = "time = " + delayed.time() + ", timeUnit = " + timeUnit;
        creator.log("Added delayed: " + manifest.getName() + " { " + scheduledMeta + " }");
        creator.increaseStatistics("delayed", 1);

        return object;
    }
}
