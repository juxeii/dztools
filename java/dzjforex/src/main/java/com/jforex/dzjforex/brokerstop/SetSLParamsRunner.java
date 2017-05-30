package com.jforex.dzjforex.brokerstop;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.OrderUtil;

import io.reactivex.Completable;
import io.reactivex.Single;

public class SetSLParamsRunner {

    private final OrderUtil orderUtil;
    private final SetSLParamsFactory orderSetSLParams;

    public SetSLParamsRunner(final OrderUtil orderUtil,
                             final SetSLParamsFactory orderSetSLParams) {
        this.orderUtil = orderUtil;
        this.orderSetSLParams = orderSetSLParams;
    }

    public Completable get(final IOrder order,
                           final BrokerStopData brokerStopData) {
        return Single
            .defer(() -> orderSetSLParams.get(order, brokerStopData))
            .flatMapObservable(orderUtil::paramsToObservable)
            .ignoreElements();
    }
}
