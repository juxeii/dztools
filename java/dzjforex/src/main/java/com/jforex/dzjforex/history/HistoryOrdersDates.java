package com.jforex.dzjforex.history;

import java.time.LocalDateTime;

import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.programming.misc.DateTimeUtil;

public class HistoryOrdersDates {

    private long to;
    private long from;

    public HistoryOrdersDates(final long serverTime,
                              final PluginConfig pluginConfig) {
        init(serverTime, pluginConfig);
    }

    private void init(final long serverTime,
                      final PluginConfig pluginConfig) {
        final LocalDateTime toDate = DateTimeUtil.dateTimeFromMillis(serverTime);
        final LocalDateTime fromDate = toDate.minusDays(pluginConfig.historyOrderInDays());
        to = DateTimeUtil.millisFromDateTime(toDate);
        from = DateTimeUtil.millisFromDateTime(fromDate);
    }

    public long from() {
        return from;
    }

    public long to() {
        return to;
    }
}
