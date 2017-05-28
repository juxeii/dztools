package com.jforex.dzjforex.misc;

import com.jforex.programming.misc.DateTimeUtil;

public class TimeSpan {

    private final long from;
    private final long to;

    public TimeSpan(final long from,
                    final long to) {
        this.from = from;
        this.to = to;
    }

    public long from() {
        return from;
    }

    public long to() {
        return to;
    }

    public String formatFrom() {
        return DateTimeUtil.formatMillis(from);
    }

    public String formatTo() {
        return DateTimeUtil.formatMillis(to);
    }
}
