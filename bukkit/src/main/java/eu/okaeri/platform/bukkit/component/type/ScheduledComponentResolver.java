package eu.okaeri.platform.bukkit.component.type;

import eu.okaeri.injector.Injector;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.platform.bukkit.annotation.Scheduled;
import eu.okaeri.platform.core.component.ComponentCreator;
import eu.okaeri.platform.core.component.creator.ComponentResolver;
import eu.okaeri.platform.core.component.manifest.BeanManifest;
import eu.okaeri.platform.core.component.manifest.BeanSource;
import lombok.NonNull;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.lang.reflect.Method;

import static eu.okaeri.platform.core.component.ComponentHelper.invokeMethod;

public class ScheduledComponentResolver implements ComponentResolver {

    @Override
    public boolean supports(Class<?> type) {
        return type.getAnnotation(Scheduled.class) != null;
    }

    @Override
    public boolean supports(Method method) {
        return method.getAnnotation(Scheduled.class) != null;
    }

    @Inject private JavaPlugin plugin;
    @Inject private BukkitScheduler scheduler;

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

        if (scheduled.async()) {
            this.scheduler.runTaskTimerAsynchronously(this.plugin, runnable, delay, rate);
        } else {
            this.scheduler.runTaskTimer(this.plugin, runnable, delay, rate);
        }

        String scheduledMeta = "delay = " + delay + ", rate = " + rate + ", async = " + scheduled.async();
        creator.log("Added scheduled: " + manifest.getName() + " { " + scheduledMeta + " }");
        creator.increaseStatistics("scheduled", 1);

        return object;
    }
}
