package com.jforex.dzjforex.order;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.brokerbuy.BrokerBuyData;
import com.jforex.dzjforex.brokersell.BrokerSellData;
import com.jforex.dzjforex.brokerstop.BrokerStopData;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.task.params.TaskParamsWithType;
import com.jforex.programming.order.task.params.basic.CloseParams;
import com.jforex.programming.order.task.params.basic.SetSLParams;
import com.jforex.programming.order.task.params.basic.SubmitParams;

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

    private OrderActionResult start(final Single<? extends TaskParamsWithType> taskParams) {
        return taskParams
            .flatMapObservable(orderUtil::paramsToObservable)
            .ignoreElements()
            .andThen(Single.just(OrderActionResult.OK))
            .onErrorReturnItem(OrderActionResult.FAIL)
            .blockingGet();
    }

    public OrderActionResult startSubmit(final Instrument instrument,
                                         final BrokerBuyData brokerBuyData,
                                         final String orderLabel) {
        final Single<SubmitParams> submitParams = orderSubmitParams.get(instrument,
                                                                        brokerBuyData,
                                                                        orderLabel);
        return start(submitParams);
    }

    public OrderActionResult startClose(final IOrder order,
                                        final BrokerSellData brokerSellData) {
        final Single<CloseParams> closeParams = orderCloseParams.get(order, brokerSellData);
        return start(closeParams);
    }

    public OrderActionResult startSetSL(final IOrder order,
                                        final BrokerStopData brokerStopData) {
        final Single<SetSLParams> setSLParams = orderSetSLParams.get(order, brokerStopData);
        return start(setSLParams);
    }
}
