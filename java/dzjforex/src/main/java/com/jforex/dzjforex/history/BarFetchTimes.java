package com.jforex.dzjforex.history;

public class BarFetchTimes {

    private final long startTime;
    private final long endTime;

    public BarFetchTimes(final long startTime,
                         final long endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public long startTime() {
        return startTime;
    }

    public long endTime() {
        return endTime;
    }
}
