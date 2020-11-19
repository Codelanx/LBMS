package edu.rit.codelanx.util;

import com.codelanx.commons.util.Parallel;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.PriorityQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A utility class for timing events outside of the user's control based on the
 * current, real time (offset by a given amount if desired). As it's a clock,
 * it of course only tracks things in the duration of a day! I certainly
 * don't have any 48-hour clocks.
 *
 * @author sja9291  Spencer Alderman
 */
public class Clock {

    //our thread pool for timing events
    private static final ScheduledExecutorService TICKER;
    //how often to check the passage of time
    private static final long CHECK_INTERVAL_MS = 500;
    //The offset (in hours) from the current time
    private long timeOffsetHours = 0;
    //A task currently executing/waiting for events
    private ScheduledFuture<?> currentTimer;
    private final AtomicLong lastSecondTick = new AtomicLong();
    private final Deque<RegisteredTask> expiredEvents = new ArrayDeque<>();
    private final PriorityQueue<RegisteredTask> events = new PriorityQueue<>();
    private final ReadWriteLock eventLock = new ReentrantReadWriteLock();

    static {
        TICKER = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setName("clock-thread");
            return t;
        });
    }

    /**
     * Begins the timer on a new {@link Clock}, firing events immediately if
     * necessary without blocking the calling thread
     */
    public Clock() {
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

    public RegisteredTask registerTask(int hours, int minutes, Runnable task) {
        return this.registerTask(hours, minutes, 0, task);
    }

    public RegisteredTask registerTask(int hours, int minutes, int seconds, Runnable task) {
        return this.registerTask(((hours * 60) + minutes) * 60 + seconds, task);
    }

    public RegisteredTask registerTask(int secondsOfDay, Runnable task) {
        RegisteredTask back;
        this.events.add(back = new RegisteredTask(secondsOfDay, task));
        return back;
    }

    //our "event loop"
    private void tick() {
        try {
            int now = this.getCurrentTime().atZone(ZoneOffset.systemDefault()).get(ChronoField.SECOND_OF_DAY);
            if (now - this.lastSecondTick.getAndSet(now) < 0) { //no time has passed, or physics broke
                Parallel.operateLock(this.eventLock.writeLock(), () -> {
                    this.events.addAll(this.expiredEvents);
                    this.expiredEvents.clear();
                });
            }
            if (this.events.isEmpty()) return;
            RegisteredTask next = this.events.peek();
            if (next == null) {
                return;
            }
            if (next.secondsOfDay > now) {
                return;
            }
            RegisteredTask found = Parallel.operateLock(this.eventLock.writeLock(), this.events::poll);
            if (found != next) {
                //we had a race condition
                Parallel.operateLock(this.eventLock.writeLock(), () -> this.events.add(found)); //whoops
                return; //try again
            }
            found.run();
        } catch (Throwable fucker) {
            Errors.reportAndExit(fucker);
        }
    }

    public class RegisteredTask implements Comparable<RegisteredTask>, Runnable {

        private final Runnable task;
        private final int secondsOfDay;

        public RegisteredTask(int secondsOfDay, Runnable task) {
            this.secondsOfDay = secondsOfDay;
            this.task = task;
        }

        @Override
        public void run() {
            this.task.run();
        }

        @Override
        public int compareTo(RegisteredTask o) {
            return Integer.compare(this.secondsOfDay, o.secondsOfDay);
        }

        public boolean cancel() {
            return Clock.this.expiredEvents.remove(this)
                    || Clock.this.events.remove(this);
        }
    }
}
