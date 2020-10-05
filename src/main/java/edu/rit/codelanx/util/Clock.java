package edu.rit.codelanx.util;

import edu.rit.codelanx.data.state.types.Library;
import edu.rit.codelanx.network.server.Server;

import java.time.Instant;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Clock {

    private static final ScheduledExecutorService TICKER = Executors.newSingleThreadScheduledExecutor();
    private static final long CHECK_INTERVAL = 500; //milliseconds
    private static final int CLOSE_TIME_24HR = 19;
    private static final int OPEN_TIME_24HR = 8;
    private final AtomicBoolean open = new AtomicBoolean(true);
    private final Server<?> server;
    private long timeOffsetHours = 0;
    private ScheduledFuture<?> currentTimer;

    public Clock(Server<?> server) {
        this.server = server;
        this.currentTimer = TICKER.scheduleAtFixedRate(this::tick, 0, CHECK_INTERVAL, TimeUnit.MILLISECONDS);
    }

    private void tick() {
        int hour = this.getCurrentTime().get(ChronoField.HOUR_OF_DAY);
        boolean shouldBeOpen = hour >= OPEN_TIME_24HR && hour < CLOSE_TIME_24HR;
        if (shouldBeOpen && this.open.compareAndSet(false, true)) {
            this.server.getDataStorage().ofLoaded(Library.class).forEach(Library::open);
        } else if (!shouldBeOpen && this.open.compareAndSet(true, false)) {
            this.server.getDataStorage().ofLoaded(Library.class).forEach(Library::close);
        }
    }

    //TODO: Improvement upon the above, decouples the Library from Clock
    //supply events to a queue
    //place in PriorityQueue based on Duration, not instant or ChronoField
    public void addEvent(ChronoField compare, int value, Runnable onEvent) {
        Instant now = this.getCurrentTime();
        int field = now.get(compare);
    }

    public void stop() {
        this.currentTimer.cancel(true);
    }

    public void resume() {
        Validate.isTrue(this.currentTimer != null, "Cannot resume a running clock!", IllegalArgumentException.class);
        this.currentTimer = TICKER.scheduleAtFixedRate(this::tick, 0, CHECK_INTERVAL, TimeUnit.MILLISECONDS);
    }

    public void advanceTime(int days) {
        this.advanceTime(days, 0);
    }

    public void advanceTime(int days, int hours) {
        this.timeOffsetHours += (days * 24) + hours;
    }

    public Instant getCurrentTime() {
        return Instant.now().plus(this.timeOffsetHours, ChronoUnit.HOURS);
    }
}
