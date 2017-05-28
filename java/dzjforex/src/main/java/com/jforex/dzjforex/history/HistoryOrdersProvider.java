package com.jforex.dzjforex.history;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.brokersubscribe.BrokerSubscribe;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.dzjforex.misc.RxUtility;
import com.jforex.dzjforex.misc.TimeSpan;

import io.reactivex.Observable;
import io.reactivex.Single;

public class HistoryOrdersProvider {

    private final HistoryWrapper historyWrapper;
    private final BrokerSubscribe brokerSubscribe;
    private final HistoryOrdersDates historyOrdersDates;
    private final PluginConfig pluginConfig;

    private final static Logger logger = LogManager.getLogger(HistoryOrders.class);

    public HistoryOrdersProvider(final HistoryWrapper historyWrapper,
                                 final BrokerSubscribe brokerSubscribe,
                                 final HistoryOrdersDates historyOrdersDates,
                                 final PluginConfig pluginConfig) {
        this.historyWrapper = historyWrapper;
        this.brokerSubscribe = brokerSubscribe;
        this.historyOrdersDates = historyOrdersDates;
        this.pluginConfig = pluginConfig;
    }

    public Single<List<IOrder>> get() {
        return Single
            .defer(historyOrdersDates::timeSpan)
            .flatMap(this::getForTimeSpan)
            .retryWhen(RxUtility.retryForHistory(pluginConfig));
    }

    private Single<List<IOrder>> getForTimeSpan(final TimeSpan timeSpan) {
        return Observable
            .fromIterable(brokerSubscribe.subscribedInstruments())
            .flatMapSingle(instrument -> ordersForInstrument(instrument, timeSpan))
            .flatMapIterable(orders -> orders)
            .toList();
    }

    private Single<List<IOrder>> ordersForInstrument(final Instrument instrument,
                                                     final TimeSpan timeSpan) {
        return historyWrapper.getOrdersHistory(instrument, timeSpan)
            .doOnSubscribe(d -> logger.debug("Fetching history orders for " + instrument
                    + " from " + timeSpan.formatFrom()
                    + " to " + timeSpan.formatTo()))
            .doOnSuccess(orders -> logger.debug("Fetched " + orders.size() + " history orders for " + instrument));
    }
}
