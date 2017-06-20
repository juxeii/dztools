package com.jforex.dzjforex.brokersell;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.OrderUtil;

import io.reactivex.Completable;
import io.reactivex.Single;

public class CloseParamsRunner {

    private final OrderUtil orderUtil;
    private final CloseParamsFactory closeParamsFactory;

    public CloseParamsRunner(final OrderUtil orderUtil,
                             final CloseParamsFactory closeParamsFactory) {
        this.orderUtil = orderUtil;
        this.closeParamsFactory = closeParamsFactory;
    }

    public Completable get(final IOrder order,
                           final BrokerSellData brokerSellData) {
        return Single
            .defer(() -> closeParamsFactory.get(order, brokerSellData))
            .flatMapObservable(orderUtil::paramsToObservable)
            .ignoreElements();
    }
}
