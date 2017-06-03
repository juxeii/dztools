package com.jforex.dzjforex.brokerbuy;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.event.OrderEvent;

import io.reactivex.Single;

public class SubmitParamsRunner {

    private final OrderUtil orderUtil;
    private final SubmitParamsFactory submitParamsFactory;

    public SubmitParamsRunner(final OrderUtil orderUtil,
                              final SubmitParamsFactory submitParamsFactory) {
        this.orderUtil = orderUtil;
        this.submitParamsFactory = submitParamsFactory;
    }

    public Single<IOrder> get(final Instrument instrument,
                              final BrokerBuyData brokerBuyData) {
        return Single
            .defer(() -> submitParamsFactory.get(instrument, brokerBuyData))
            .flatMapObservable(orderUtil::paramsToObservable)
            .lastOrError()
            .map(OrderEvent::order);
    }
}
