package eu.okaeri.platform.bukkit.commons.time;

import java.util.concurrent.TimeUnit;

// utility exposing easy ready-to-use time->ticks values
// remember the final time is dependant on the server performance (tps)
@SuppressWarnings("PointlessArithmeticExpression")
public final class MinecraftTimeEquivalent {

    public static final int SECONDS_1 = 1 * 20;
    public static final int SECONDS_2 = 2 * 20;
    public static final int SECONDS_3 = 3 * 20;
    public static final int SECONDS_4 = 4 * 20;
    public static final int SECONDS_5 = 5 * 20;
    public static final int SECONDS_6 = 6 * 20;
    public static final int SECONDS_7 = 7 * 20;
    public static final int SECONDS_8 = 8 * 20;
    public static final int SECONDS_9 = 9 * 20;
    public static final int SECONDS_10 = 10 * 20;
    public static final int SECONDS_15 = 15 * 20;
    public static final int SECONDS_20 = 20 * 20;
    public static final int SECONDS_25 = 25 * 20;
    public static final int SECONDS_30 = 30 * 20;
    public static final int SECONDS_35 = 35 * 20;
    public static final int SECONDS_40 = 40 * 20;
    public static final int SECONDS_45 = 45 * 20;
    public static final int SECONDS_50 = 50 * 20;
    public static final int SECONDS_55 = 55 * 20;
    public static final int SECONDS_60 = 60 * 20;

    public static final int MINUTES_1 = 1 * 60 * 20;
    public static final int MINUTES_2 = 2 * 60 * 20;
    public static final int MINUTES_3 = 3 * 60 * 20;
    public static final int MINUTES_4 = 4 * 60 * 20;
    public static final int MINUTES_5 = 5 * 60 * 20;
    public static final int MINUTES_6 = 6 * 60 * 20;
    public static final int MINUTES_7 = 7 * 60 * 20;
    public static final int MINUTES_8 = 8 * 60 * 20;
    public static final int MINUTES_9 = 9 * 60 * 20;
    public static final int MINUTES_10 = 10 * 60 * 20;
    public static final int MINUTES_15 = 15 * 60 * 20;
    public static final int MINUTES_20 = 20 * 60 * 20;
    public static final int MINUTES_25 = 25 * 60 * 20;
    public static final int MINUTES_30 = 30 * 60 * 20;
    public static final int MINUTES_35 = 35 * 60 * 20;
    public static final int MINUTES_40 = 40 * 60 * 20;
    public static final int MINUTES_45 = 45 * 60 * 20;
    public static final int MINUTES_50 = 50 * 60 * 20;
    public static final int MINUTES_55 = 55 * 60 * 20;
    public static final int MINUTES_60 = 60 * 60 * 20;

    public static final int HOURS_1 = 1 * 60 * 60 * 20;
    public static final int HOURS_2 = 2 * 60 * 60 * 20;
    public static final int HOURS_3 = 3 * 60 * 60 * 20;
    public static final int HOURS_4 = 4 * 60 * 60 * 20;
    public static final int HOURS_5 = 5 * 60 * 60 * 20;
    public static final int HOURS_6 = 6 * 60 * 60 * 20;
    public static final int HOURS_7 = 7 * 60 * 60 * 20;
    public static final int HOURS_8 = 8 * 60 * 60 * 20;
    public static final int HOURS_9 = 9 * 60 * 60 * 20;
    public static final int HOURS_10 = 10 * 60 * 60 * 20;
    public static final int HOURS_11 = 11 * 60 * 60 * 20;
    public static final int HOURS_12 = 12 * 60 * 60 * 20;
    public static final int HOURS_13 = 13 * 60 * 60 * 20;
    public static final int HOURS_14 = 14 * 60 * 60 * 20;
    public static final int HOURS_15 = 15 * 60 * 60 * 20;
    public static final int HOURS_16 = 16 * 60 * 60 * 20;
    public static final int HOURS_17 = 17 * 60 * 60 * 20;
    public static final int HOURS_18 = 18 * 60 * 60 * 20;
    public static final int HOURS_19 = 19 * 60 * 60 * 20;
    public static final int HOURS_20 = 20 * 60 * 60 * 20;
    public static final int HOURS_21 = 21 * 60 * 60 * 20;
    public static final int HOURS_22 = 22 * 60 * 60 * 20;
    public static final int HOURS_23 = 23 * 60 * 60 * 20;
    public static final int HOURS_24 = 24 * 60 * 60 * 20;

    public static int ticksOf(TimeUnit timeUnit, int value) {
        long millis = timeUnit.toMillis(value);
        if (millis < 50) throw new IllegalArgumentException("cannot transform " + millis + " ms to ticks, too low value");
        return Math.toIntExact(millis / 50L);
    }
}
