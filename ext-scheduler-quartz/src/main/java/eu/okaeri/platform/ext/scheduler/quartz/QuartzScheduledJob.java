package eu.okaeri.platform.ext.scheduler.quartz;

import lombok.NoArgsConstructor;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.logging.Level;
import java.util.logging.Logger;

@NoArgsConstructor
public class QuartzScheduledJob implements Job {

    private static final Logger LOGGER = Logger.getLogger(QuartzScheduledJob.class.getSimpleName());

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        JobDataMap jobDataMap = jobExecutionContext.getJobDetail().getJobDataMap();
        int runnableKey = jobDataMap.getInt("runnable");
        String name = jobDataMap.getString("name");

        try {
            QuartzScheduledResolver.JOBS.get(runnableKey).run();
        }
        catch (Exception exception) {
            LOGGER.log(Level.WARNING, "failed to execute job [name: " + name + ", hashCode: " + runnableKey + "]", exception);
        }
    }
}
