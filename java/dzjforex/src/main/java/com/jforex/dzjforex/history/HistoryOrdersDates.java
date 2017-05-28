package com.jforex.dzjforex.history;

import java.time.LocalDateTime;

import com.jforex.dzjforex.brokertime.ServerTimeProvider;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.dzjforex.misc.TimeSpan;
import com.jforex.programming.misc.DateTimeUtil;

import io.reactivex.Single;

public class HistoryOrdersDates {

    private final ServerTimeProvider serverTimeProvider;
    private final PluginConfig pluginConfig;

    public HistoryOrdersDates(final ServerTimeProvider serverTimeProvider,
                              final PluginConfig pluginConfig) {
        this.serverTimeProvider = serverTimeProvider;
        this.pluginConfig = pluginConfig;
    }

    public Single<TimeSpan> timeSpan() {
        return Single
            .defer(serverTimeProvider::get)
            .map(this::timeSpanForServerTime);
    }

    private TimeSpan timeSpanForServerTime(final long serverTime) {
        return new TimeSpan(fromMillis(serverTime), serverTime);
    }

    private long fromMillis(final long serverTime) {
        final LocalDateTime fromDate = DateTimeUtil
            .dateTimeFromMillis(serverTime)
            .minusDays(pluginConfig.historyOrderInDays());

        return DateTimeUtil.millisFromDateTime(fromDate);
    }
}
