package com.jforex.dzjforex.brokerstop;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.OrderUtil;

import io.reactivex.Completable;
import io.reactivex.Single;

public class SetSLParamsRunner {

    private final OrderUtil orderUtil;
    private final SetSLParamsFactory setSLParamsFactory;

    public SetSLParamsRunner(final OrderUtil orderUtil,
                             final SetSLParamsFactory setSLParamsFactory) {
        this.orderUtil = orderUtil;
        this.setSLParamsFactory = setSLParamsFactory;
    }

    public Completable get(final IOrder order,
                           final BrokerStopData brokerStopData) {
        return Single
            .defer(() -> setSLParamsFactory.get(order, brokerStopData))
            .flatMapObservable(orderUtil::paramsToObservable)
            .ignoreElements();
    }
}
