package com.jforex.dzjforex.brokertime;

import java.time.Clock;

public class TimeWatch {

    private final Clock clock;
    private long latestTime;
    private long synchTime;

    public TimeWatch(final Clock clock) {
        this.clock = clock;
    }

    public long getForNewTime(final long newTime) {
        final long timeWithOffset = getWithOffset();
        if (newTime > timeWithOffset) {
            synch(newTime);
            return newTime;
        }
        return timeWithOffset;
    }

    private void synch(final long latestTime) {
        this.latestTime = latestTime;
        synchTime = currentTime();
    }

    private long getWithOffset() {
        final long diffToSynchTime = currentTime() - synchTime;
        return latestTime + diffToSynchTime;
    }

    private long currentTime() {
        return clock.millis();
    }
}
