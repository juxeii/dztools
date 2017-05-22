package com.jforex.dzjforex.brokersell;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.OrderUtil;

import io.reactivex.Completable;

public class CloseParamsRunner {

    private final OrderUtil orderUtil;
    private final OrderCloseParams orderCloseParams;

    public CloseParamsRunner(final OrderUtil orderUtil,
                             final OrderCloseParams orderCloseParams) {
        this.orderUtil = orderUtil;
        this.orderCloseParams = orderCloseParams;
    }

    public Completable get(final IOrder order,
                           final BrokerSellData brokerSellData) {
        return orderCloseParams
            .get(order, brokerSellData)
            .flatMapObservable(orderUtil::paramsToObservable)
            .ignoreElements();
    }
}
