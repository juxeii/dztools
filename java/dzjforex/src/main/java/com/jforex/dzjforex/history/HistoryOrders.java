package com.jforex.dzjforex.history;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.jforex.dzjforex.brokerapi.BrokerSubscribe;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.dzjforex.time.ServerTimeProvider;
import com.jforex.programming.misc.DateTimeUtil;

import io.reactivex.Observable;
import io.reactivex.Single;

public class HistoryOrders {

    private final HistoryProvider historyProvider;
    private final BrokerSubscribe brokerSubscribe;
    private final PluginConfig pluginConfig;
    private final ServerTimeProvider serverTimeProvider;

    private final static Logger logger = LogManager.getLogger(HistoryOrders.class);

    public HistoryOrders(final HistoryProvider historyProvider,
                         final BrokerSubscribe brokerSubscribe,
                         final PluginConfig pluginConfig,
                         final ServerTimeProvider serverTimeProvider) {
        this.historyProvider = historyProvider;
        this.brokerSubscribe = brokerSubscribe;
        this.pluginConfig = pluginConfig;
        this.serverTimeProvider = serverTimeProvider;
    }

    public Single<List<IOrder>> get() {
        final LocalDateTime toDate = DateTimeUtil.dateTimeFromMillis(serverTimeProvider.get());
        final LocalDateTime fromDate = toDate.minusDays(pluginConfig.historyOrderInDays());
        final long to = DateTimeUtil.millisFromDateTime(toDate);
        final long from = DateTimeUtil.millisFromDateTime(fromDate);

        return Observable
            .fromIterable(brokerSubscribe.subscribedInstruments())
            .doOnSubscribe(d -> logger.debug("Fetching history orders for " + brokerSubscribe.subscribedInstruments()
                    + " from " + DateTimeUtil.formatMillis(from)
                    + " to " + DateTimeUtil.formatMillis(to)))
            .flatMapSingle(instrument -> historyProvider.ordersByInstrument(instrument,
                                                                            from,
                                                                            to))
            .flatMapIterable(orders -> orders)
            .toList()
            .doOnSuccess(orders -> logger.debug("Fetched " + orders.size()
                    + " history orders for " + brokerSubscribe.subscribedInstruments()));
    }
}
