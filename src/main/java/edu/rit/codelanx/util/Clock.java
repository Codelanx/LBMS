package edu.rit.codelanx.util;

import edu.rit.codelanx.data.state.types.Library;
import edu.rit.codelanx.network.server.Server;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A utility class for timing events outside of the user's control based on the
 * current, real time (offset by a given amount if desired)
 *
 * @author sja9291  Spencer Alderman
 */
public class Clock {

    //our thread pool for timing events
    private static final ScheduledExecutorService TICKER;
    //how often to check the passage of time
    private static final long CHECK_INTERVAL_MS = 500;
    //REFACTOR: when to close the Library
    private static final int CLOSE_TIME_24HR = 19;
    //REFACTOR: when to open the library
    private static final int OPEN_TIME_24HR = 8;
    //REFACTOR: If the clock has run the open/close seq
    private final AtomicBoolean open = new AtomicBoolean(false);
    //The server to execute events on
    private final Server<?> server;
    //The offset (in hours) from the current time
    private long timeOffsetHours = 0;
    //A task currently executing/waiting for events
    private ScheduledFuture<?> currentTimer;

    static {
        TICKER = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * Begins the timer on a new {@link Clock}, firing events immediately if
     * necessary without blocking the calling thread
     *
     * @param server The {@link Server} to fire events on
     */
    public Clock(Server<?> server) {
        this.server = server;
        this.resume();
    }

    /**
     * Stop the internal timer, effectively pausing any calls to events
     */
    public void stop() {
        if (this.currentTimer.cancel(true)) {
            this.currentTimer = null;
        } else {
            throw new IllegalStateException("Clock is already stopped");
        }
    }

    /**
     * Resume the event timer if it was stopped
     */
    public void resume() {
        Validate.isTrue(this.currentTimer == null, "Cannot resume a running clock!", IllegalArgumentException.class);
        this.currentTimer = TICKER.scheduleAtFixedRate(this::tick, 0, CHECK_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    /**
     * Whether the internal timer is currently executing events
     *
     * @return {@code true} if the timer is running, {@code false} otherwise
     */
    public boolean isRunning() {
        return this.currentTimer != null
                && !this.currentTimer.isDone()
                && !this.currentTimer.isCancelled();
    }

    /**
     * Advances the reference point for the "current time" by the given
     * parameters, such that any new reference NOW would become NOW+(params)
     * 
     * @param days The number of days to offset the current time
     * @see #advanceTime(int, int)
     */
    public void advanceTime(int days) {
        this.advanceTime(days, 0);
    }

    /**
     * Advances the reference point for the "current time" by the given
     * parameters, such that any new reference NOW would become NOW+(params)
     *
     * @param days The number of days to offset the current time
     * @param hours The number of hours to offset the current time
     * @see #advanceTime(int)
     */
    public void advanceTime(int days, int hours) {
        this.timeOffsetHours += (days * 24) + hours;
    }

    /**
     * Returns the "current time", including any calculated offsets that have
     * been provided to the clock throughout the runtime of the program
     *
     * TODO: Do we persist a clock-ahead? I think it'd present with data
     *    inconsistencies if not
     *
     * @return An {@link Instant} describing the current+offset time
     */
    public Instant getCurrentTime() {
        return Instant.now().plus(this.timeOffsetHours, ChronoUnit.HOURS);
    }

    //REFACTOR:
    // This should utilize an #addEvent or some such, to completely
    // decouple Clock from Library
    //our "event loop"
    private void tick() {
        int hour = LocalDateTime.ofInstant(this.getCurrentTime(), ZoneId.systemDefault()).getHour();
        boolean shouldBeOpen = hour >= OPEN_TIME_24HR && hour < CLOSE_TIME_24HR;
        if (shouldBeOpen && this.open.compareAndSet(false, true)) {
            this.server.getLibraryData().ofLoaded(Library.class).forEach(Library::open);
        } else if (!shouldBeOpen && this.open.compareAndSet(true, false)) {
            this.server.getLibraryData().ofLoaded(Library.class).forEach(Library::close);
        }
    }
}
