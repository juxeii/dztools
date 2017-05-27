package com.jforex.dzjforex.history;

import java.time.LocalDateTime;

import com.jforex.dzjforex.brokertime.ServerTimeProvider;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.programming.misc.DateTimeUtil;

import io.reactivex.Completable;
import io.reactivex.Single;

public class HistoryOrdersDates {

    private final ServerTimeProvider serverTimeProvider;
    private final PluginConfig pluginConfig;
    private long to;
    private long from;

    public HistoryOrdersDates(final ServerTimeProvider serverTimeProvider,
                              final PluginConfig pluginConfig) {
        this.pluginConfig = pluginConfig;
        this.serverTimeProvider = serverTimeProvider;
    }

    public Completable initNewDates() {
        return Single
            .defer(serverTimeProvider::get)
            .flatMapCompletable(this::initWithServerTime);
    }

    private Completable initWithServerTime(final long serverTime) {
        return Completable.fromAction(() -> {
            final LocalDateTime toDate = DateTimeUtil.dateTimeFromMillis(serverTime);
            final LocalDateTime fromDate = toDate.minusDays(pluginConfig.historyOrderInDays());
            to = DateTimeUtil.millisFromDateTime(toDate);
            from = DateTimeUtil.millisFromDateTime(fromDate);
        });
    }

    public long to() {
        return to;
    }

    public long from() {
        return from;
    }
}
