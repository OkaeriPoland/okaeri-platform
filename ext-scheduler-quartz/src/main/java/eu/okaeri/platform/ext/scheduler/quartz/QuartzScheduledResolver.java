package eu.okaeri.platform.ext.scheduler.quartz;

import eu.okaeri.injector.Injector;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.platform.core.component.ComponentHelper;
import eu.okaeri.platform.core.component.creator.ComponentCreator;
import eu.okaeri.platform.core.component.creator.ComponentResolver;
import eu.okaeri.platform.core.component.manifest.BeanManifest;
import eu.okaeri.platform.core.component.manifest.BeanSource;
import eu.okaeri.platform.core.exception.BreakException;
import lombok.NonNull;
import org.quartz.*;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class QuartzScheduledResolver implements ComponentResolver {

    private static final Map<String, String> CRON_MAPPINGS = new HashMap<>();
    static final Map<Integer, Runnable> JOBS = new ConcurrentHashMap<>();

    static {
        CRON_MAPPINGS.put("@yearly", QuartzScheduledTime.YEARLY);
        CRON_MAPPINGS.put("@annually", QuartzScheduledTime.ANNUALLY);
        CRON_MAPPINGS.put("@monthly", QuartzScheduledTime.MONTHLY);
        CRON_MAPPINGS.put("@weekly", QuartzScheduledTime.WEEKLY);
        CRON_MAPPINGS.put("@daily", QuartzScheduledTime.DAILY);
        CRON_MAPPINGS.put("@midnight", QuartzScheduledTime.MIDNIGHT);
        CRON_MAPPINGS.put("@hourly", QuartzScheduledTime.HOURLY);
    }

    private final Scheduler scheduler;

    @Inject
    public QuartzScheduledResolver(@NonNull Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public boolean supports(@NonNull Class<?> type) {
        return type.getAnnotation(QuartzScheduled.class) != null;
    }

    @Override
    public boolean supports(@NonNull Method method) {
        return method.getAnnotation(QuartzScheduled.class) != null;
    }

    @Override
    public Object make(@NonNull ComponentCreator creator, @NonNull BeanManifest manifest, @NonNull Injector injector) {

        long start = System.currentTimeMillis();
        Runnable runnable = ComponentHelper.manifestToRunnable(manifest, injector);

        QuartzScheduled scheduled = manifest.getSource() == BeanSource.METHOD
            ? manifest.getMethod().getAnnotation(QuartzScheduled.class)
            : manifest.getType().getAnnotation(QuartzScheduled.class);

        if (!scheduled.name().isEmpty()) {
            manifest.setName(scheduled.name());
        }

        String expression = CRON_MAPPINGS.getOrDefault(scheduled.cron(), scheduled.cron());
        String name = manifest.getSource() == BeanSource.METHOD ? manifest.getName() : manifest.getType().getSimpleName();

        // :(
        Runnable conflictingJob = JOBS.put(runnable.hashCode(), runnable);
        if (conflictingJob != null) {
            throw new BreakException("conflicting hashCode for job " + name + " [first: " + conflictingJob + ", second: " + runnable + "]");
        }

        JobDetail jobDetail = JobBuilder.newJob()
            .ofType(QuartzScheduledJob.class)
            .withIdentity(name)
            .usingJobData("name", name)
            .usingJobData("runnable", runnable.hashCode())
            .build();

        CronTrigger cronTrigger = TriggerBuilder.newTrigger()
            .forJob(jobDetail)
            .withIdentity(name + " trigger")
            .withSchedule(CronScheduleBuilder.cronSchedule(expression))
            .build();

        try {
            this.scheduler.scheduleJob(jobDetail, cronTrigger);
        } catch (SchedulerException exception) {
            throw new BreakException("failed scheduleJob", exception);
        }

        long took = System.currentTimeMillis() - start;
        creator.log(ComponentHelper.buildComponentMessage()
            .type("Added cron (quartz)")
            .name(name)
            .took(took)
            .meta("value", expression)
            .build());
        creator.increaseStatistics("cron", 1);

        return runnable;
    }
}
