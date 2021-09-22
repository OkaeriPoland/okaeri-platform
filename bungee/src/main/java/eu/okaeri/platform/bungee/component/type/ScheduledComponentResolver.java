package eu.okaeri.platform.bungee.component.type;

import eu.okaeri.injector.Injector;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.platform.bungee.annotation.Scheduled;
import eu.okaeri.platform.bungee.scheduler.PlatformScheduler;
import eu.okaeri.platform.core.component.creator.ComponentCreator;
import eu.okaeri.platform.core.component.creator.ComponentResolver;
import eu.okaeri.platform.core.component.manifest.BeanManifest;
import eu.okaeri.platform.core.component.manifest.BeanSource;
import lombok.NonNull;
import net.md_5.bungee.api.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import static eu.okaeri.platform.core.component.ComponentHelper.invokeMethod;

public class ScheduledComponentResolver implements ComponentResolver {

    @Override
    public boolean supports(@NonNull Class<?> type) {
        return type.getAnnotation(Scheduled.class) != null;
    }

    @Override
    public boolean supports(@NonNull Method method) {
        return method.getAnnotation(Scheduled.class) != null;
    }

    @Inject private Plugin plugin;
    @Inject private PlatformScheduler scheduler;

    @Override
    public Object make(@NonNull ComponentCreator creator, @NonNull BeanManifest manifest, @NonNull Injector injector) {

        Object object;
        if (manifest.getSource() == BeanSource.METHOD) {
            object = (Runnable) () -> invokeMethod(manifest, injector);
            manifest.setName(manifest.getMethod().getName());
        } else {
            if (!Runnable.class.isAssignableFrom(manifest.getType())) {
                throw new IllegalArgumentException("Component of @Scheduled on type requires class to be a java.lang.Runnable: " + manifest);
            }
            object = injector.createInstance(manifest.getType());
        }

        Runnable runnable = (Runnable) object;
        Scheduled scheduled = manifest.getSource() == BeanSource.METHOD
                ? manifest.getMethod().getAnnotation(Scheduled.class)
                : manifest.getType().getAnnotation(Scheduled.class);

        if (!scheduled.name().isEmpty()) {
            manifest.setName(scheduled.name());
        }

        int rate = scheduled.rate();
        int delay = (scheduled.delay() == -1) ? rate : scheduled.delay();
        TimeUnit timeUnit = scheduled.timeUnit();

        this.scheduler.schedule(runnable, delay, rate, timeUnit);

        String scheduledMeta = "delay = " + delay + ", rate = " + rate + ", timeUnit = " + scheduled.timeUnit();
        if (manifest.getSource() == BeanSource.METHOD) {
            creator.log("Added scheduled: " + manifest.getName() + " { " + scheduledMeta + " }");
        } else {
            creator.log("Added scheduled: " + manifest.getType().getSimpleName() + " { " + scheduledMeta + " }");
        }
        creator.increaseStatistics("scheduled", 1);

        return object;
    }
}
