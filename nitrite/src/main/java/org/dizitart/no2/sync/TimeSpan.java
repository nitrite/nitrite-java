package org.dizitart.no2.sync;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.TimeUnit;

/**
 * Represents a time span.
 *
 * @since 1.0
 * @author Anindya Chatterjee
 */
@Getter @Setter
public class TimeSpan {

    /**
     * The time.
     *
     * @param time the time
     * @return the time.
     * */
    private long time;

    /**
     * The time unit.
     *
     * @param time the time unit
     * @return the time unit.
     * */
    private TimeUnit timeUnit;

    /**
     * Instantiates a new {@link TimeSpan}.
     *
     * @param time     the time
     * @param timeUnit the time unit
     */
    public TimeSpan(long time, TimeUnit timeUnit) {
        this.time = time;
        this.timeUnit = timeUnit;
    }

    /**
     * Creates a new {@link TimeSpan}.
     *
     * @param time     the time
     * @param timeUnit the time unit
     * @return the time span
     */
    public static TimeSpan timeSpan(long time, TimeUnit timeUnit) {
        return new TimeSpan(time, timeUnit);
    }
}
