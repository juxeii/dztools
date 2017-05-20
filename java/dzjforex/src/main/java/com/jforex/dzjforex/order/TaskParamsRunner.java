package com.jforex.dzjforex.order;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.brokerbuy.BrokerBuyData;
import com.jforex.dzjforex.brokerbuy.OrderSubmitParams;
import com.jforex.dzjforex.brokersell.BrokerSellData;
import com.jforex.dzjforex.brokersell.OrderCloseParams;
import com.jforex.dzjforex.brokerstop.BrokerStopData;
import com.jforex.dzjforex.brokerstop.OrderSetSLParams;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.event.OrderEvent;

import io.reactivex.Completable;
import io.reactivex.Single;

public class TaskParamsRunner {

    private final OrderUtil orderUtil;
    private final OrderSubmitParams orderSubmitParams;
    private final OrderCloseParams orderCloseParams;
    private final OrderSetSLParams orderSetSLParams;

    public TaskParamsRunner(final OrderUtil orderUtil,
                            final OrderSubmitParams orderSubmitParams,
                            final OrderCloseParams orderCloseParams,
                            final OrderSetSLParams orderSetSLParams) {
        this.orderUtil = orderUtil;
        this.orderSubmitParams = orderSubmitParams;
        this.orderCloseParams = orderCloseParams;
        this.orderSetSLParams = orderSetSLParams;
    }

    public Single<IOrder> startSubmit(final Instrument instrument,
                                      final BrokerBuyData brokerBuyData,
                                      final String orderLabel) {
        return orderSubmitParams
            .get(instrument,
                 brokerBuyData,
                 orderLabel)
            .flatMapObservable(orderUtil::paramsToObservable)
            .map(OrderEvent::order)
            .lastOrError();
    }

    public Completable startClose(final IOrder order,
                                  final BrokerSellData brokerSellData) {
        return orderCloseParams
            .get(order, brokerSellData)
            .flatMapObservable(orderUtil::paramsToObservable)
            .ignoreElements();
    }

    public Completable startSetSL(final IOrder order,
                                  final BrokerStopData brokerStopData) {
        return orderSetSLParams
            .get(order, brokerStopData)
            .flatMapObservable(orderUtil::paramsToObservable)
            .ignoreElements();
    }
}
