package eu.okaeri.platform.ext.scheduler.quartz;

public interface QuartzScheduledTime {
    String YEARLY = "0 0 0 1 1 ?";
    String ANNUALLY = YEARLY;
    String MONTHLY = "0 0 0 1 * ?";
    String WEEKLY = "0 0 0 ? * 0";
    String DAILY = "0 0 0 * * ?";
    String MIDNIGHT = DAILY;
    String HOURLY = "0 0 * * * ?";
}
