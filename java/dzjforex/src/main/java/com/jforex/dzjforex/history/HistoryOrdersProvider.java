package com.jforex.dzjforex.history;

import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.brokersubscribe.BrokerSubscribe;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.dzjforex.misc.RxUtility;
import com.jforex.programming.misc.DateTimeUtil;

import io.reactivex.Completable;
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
        return Completable
            .defer(historyOrdersDates::initNewDates)
            .andThen(Single.defer(this::getForDates))
            .doOnSuccess(orders -> logger.debug("Fetched " + orders.size() + " history orders"))
            .retryWhen(RxUtility.retryForHistory(pluginConfig));
    }

    public Single<List<IOrder>> getForDates() {
        return Observable
            .defer(this::instrumentsForFetch)
            .flatMapSingle(this::ordersForInstrument)
            .flatMapIterable(orders -> orders)
            .toList();
    }

    public Observable<Instrument> instrumentsForFetch() {
        final Set<Instrument> subscribedInstruments = brokerSubscribe.subscribedInstruments();
        return Observable
            .fromIterable(subscribedInstruments)
            .doOnSubscribe(d -> logger.debug("Fetching history orders for " + subscribedInstruments
                    + " from " + DateTimeUtil.formatMillis(historyOrdersDates.from())
                    + " to " + DateTimeUtil.formatMillis(historyOrdersDates.to())));
    }

    public Single<List<IOrder>> ordersForInstrument(final Instrument instrument) {
        return historyWrapper.getOrdersHistory(instrument,
                                               historyOrdersDates.from(),
                                               historyOrdersDates.to());
    }
}
