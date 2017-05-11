package com.jforex.dzjforex.history;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.brokersubscribe.BrokerSubscribe;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.dzjforex.time.ServerTimeProvider;
import com.jforex.programming.misc.DateTimeUtil;

import io.reactivex.Observable;
import io.reactivex.Single;

public class HistoryOrders {

    private final HistoryWrapper historyWrapper;
    private final BrokerSubscribe brokerSubscribe;
    private final PluginConfig pluginConfig;
    private final ServerTimeProvider serverTimeProvider;

    private final static Logger logger = LogManager.getLogger(HistoryOrders.class);

    public HistoryOrders(final HistoryWrapper historyWrapper,
                         final BrokerSubscribe brokerSubscribe,
                         final PluginConfig pluginConfig,
                         final ServerTimeProvider serverTimeProvider) {
        this.historyWrapper = historyWrapper;
        this.brokerSubscribe = brokerSubscribe;
        this.pluginConfig = pluginConfig;
        this.serverTimeProvider = serverTimeProvider;
    }

    public Single<List<IOrder>> get() {
        return Single
            .defer(() -> serverTimeProvider.get())
            .flatMap(serverTime -> {
                final LocalDateTime toDate = DateTimeUtil.dateTimeFromMillis(serverTime);
                final LocalDateTime fromDate = toDate.minusDays(pluginConfig.historyOrderInDays());
                final long to = DateTimeUtil.millisFromDateTime(toDate);
                final long from = DateTimeUtil.millisFromDateTime(fromDate);
                final Set<Instrument> subscribedInstruments = brokerSubscribe.subscribedInstruments();

                return Observable
                    .fromIterable(subscribedInstruments)
                    .doOnSubscribe(d -> logger
                        .debug("Fetching history orders for " + brokerSubscribe.subscribedInstruments()
                                + " from " + DateTimeUtil.formatMillis(from)
                                + " to " + DateTimeUtil.formatMillis(to)))
                    .flatMapSingle(instrument -> historyWrapper.getOrdersHistory(instrument,
                                                                                   from,
                                                                                   to))
                    .flatMapIterable(orders -> orders)
                    .toList()
                    .doOnSuccess(orders -> logger.debug("Fetched " + orders.size()
                            + " history orders for " + subscribedInstruments));
            });
    }
}
