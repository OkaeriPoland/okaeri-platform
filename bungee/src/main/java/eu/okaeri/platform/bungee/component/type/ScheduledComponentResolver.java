package eu.okaeri.platform.bungee.component.type;

import eu.okaeri.injector.Injector;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.platform.bungee.annotation.Scheduled;
import eu.okaeri.platform.bungee.scheduler.PlatformScheduler;
import eu.okaeri.platform.core.component.ComponentHelper;
import eu.okaeri.platform.core.component.creator.ComponentCreator;
import eu.okaeri.platform.core.component.creator.ComponentResolver;
import eu.okaeri.platform.core.component.manifest.BeanManifest;
import eu.okaeri.platform.core.component.manifest.BeanSource;
import lombok.NonNull;
import net.md_5.bungee.api.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

public class ScheduledComponentResolver implements ComponentResolver {

    @Override
    public boolean supports(@NonNull Class<?> type) {
        return type.getAnnotation(Scheduled.class) != null;
    }

    @Override
    public boolean supports(@NonNull Method method) {
        return method.getAnnotation(Scheduled.class) != null;
    }

    private @Inject Plugin plugin;
    private @Inject PlatformScheduler scheduler;

    @Override
    public Object make(@NonNull ComponentCreator creator, @NonNull BeanManifest manifest, @NonNull Injector injector) {

        long start = System.currentTimeMillis();
        Runnable runnable = ComponentHelper.manifestToRunnableWithRegister(manifest, injector);

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

        long took = System.currentTimeMillis() - start;
        creator.log(ComponentHelper.buildComponentMessage()
                .type("Added scheduled")
                .name(manifest.getSource() == BeanSource.METHOD ? manifest.getName() : manifest.getType().getSimpleName())
                .took(took)
                .meta("delay", delay)
                .meta("rate", rate)
                .meta("timeUnit", timeUnit)
                .build());

        creator.increaseStatistics("scheduled", 1);

        return runnable;
    }
}
