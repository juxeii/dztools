package com.jforex.dzjforex.brokertime;

import java.time.Clock;

public class TimeWatch {

    private final Clock clock;
    private long latestTime;
    private long synchTime;

    public TimeWatch(final Clock clock) {
        this.clock = clock;
    }

    public void synch(final long latestTime) {
        this.latestTime = latestTime;
        synchTime = currentTime();
    }

    public long get() {
        final long diffToSynchTime = currentTime() - synchTime;
        return latestTime + diffToSynchTime;
    }

    private long currentTime() {
        return clock.millis();
    }
}
