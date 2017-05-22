package com.jforex.dzjforex.brokerbuy;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.event.OrderEvent;

import io.reactivex.Single;

public class SubmitParamsRunner {

    private final OrderUtil orderUtil;
    private final OrderSubmitParams orderSubmitParams;

    public SubmitParamsRunner(final OrderUtil orderUtil,
                              final OrderSubmitParams orderSubmitParams) {
        this.orderUtil = orderUtil;
        this.orderSubmitParams = orderSubmitParams;
    }

    public Single<IOrder> get(final BrokerBuyData brokerBuyData) {
        return orderSubmitParams
            .get(brokerBuyData)
            .flatMapObservable(orderUtil::paramsToObservable)
            .map(OrderEvent::order)
            .lastOrError();
    }
}
