package com.jforex.dzjforex.history;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.brokersubscribe.BrokerSubscribe;
import com.jforex.dzjforex.brokertime.ServerTimeProvider;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.dzjforex.misc.RxUtility;
import com.jforex.programming.misc.DateTimeUtil;

import io.reactivex.Observable;
import io.reactivex.Single;

public class HistoryOrdersProvider {

    private final HistoryWrapper historyWrapper;
    private final BrokerSubscribe brokerSubscribe;
    private final ServerTimeProvider serverTimeProvider;
    private final PluginConfig pluginConfig;

    private final static Logger logger = LogManager.getLogger(HistoryOrders.class);

    public HistoryOrdersProvider(final HistoryWrapper historyWrapper,
                                 final BrokerSubscribe brokerSubscribe,
                                 final ServerTimeProvider serverTimeProvider,
                                 final PluginConfig pluginConfig) {
        this.historyWrapper = historyWrapper;
        this.brokerSubscribe = brokerSubscribe;
        this.serverTimeProvider = serverTimeProvider;
        this.pluginConfig = pluginConfig;
    }

    public Single<List<IOrder>> get() {
        return Single
            .defer(serverTimeProvider::get)
            .map(serverTime -> new HistoryOrdersDates(serverTime, pluginConfig))
            .flatMap(this::getForDates)
            .retryWhen(RxUtility.retryForHistory(pluginConfig));
    }

    private Single<List<IOrder>> getForDates(final HistoryOrdersDates historyOrdersDates) {
        return Observable
            .fromIterable(brokerSubscribe.subscribedInstruments())
            .flatMapSingle(instrument -> ordersForInstrument(instrument, historyOrdersDates))
            .flatMapIterable(orders -> orders)
            .toList();
    }

    private Single<List<IOrder>> ordersForInstrument(final Instrument instrument,
                                                     final HistoryOrdersDates historyOrdersDates) {
        return historyWrapper.getOrdersHistory(instrument,
                                               historyOrdersDates.from(),
                                               historyOrdersDates.to())
            .doOnSubscribe(d -> logger.debug("Fetching history orders for " + instrument
                    + " from " + DateTimeUtil.formatMillis(historyOrdersDates.from())
                    + " to " + DateTimeUtil.formatMillis(historyOrdersDates.to())))
            .doOnSuccess(orders -> logger.debug("Fetched " + orders.size() + " history orders for " + instrument));
    }
}
