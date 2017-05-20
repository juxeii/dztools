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
import com.jforex.programming.order.task.params.TaskParamsWithType;
import com.jforex.programming.order.task.params.basic.CloseParams;
import com.jforex.programming.order.task.params.basic.SetSLParams;
import com.jforex.programming.order.task.params.basic.SubmitParams;

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
        final Single<SubmitParams> submitParams = orderSubmitParams.get(instrument,
                                                                        brokerBuyData,
                                                                        orderLabel);
        return submitParams
            .flatMapObservable(orderUtil::paramsToObservable)
            .map(OrderEvent::order)
            .lastOrError();
    }

    public Completable startClose(final IOrder order,
                                  final BrokerSellData brokerSellData) {
        final Single<CloseParams> closeParams = orderCloseParams.get(order, brokerSellData);
        return start(closeParams);
    }

    public Completable startSetSL(final IOrder order,
                                  final BrokerStopData brokerStopData) {
        final Single<SetSLParams> setSLParams = orderSetSLParams.get(order, brokerStopData);
        return start(setSLParams);
    }

    private Completable start(final Single<? extends TaskParamsWithType> taskParams) {
        return taskParams
            .flatMapObservable(orderUtil::paramsToObservable)
            .ignoreElements();
    }
}
