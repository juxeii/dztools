package com.jforex.dzjforex.brokerbuy;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.order.OrderActionResult;
import com.jforex.dzjforex.order.OrderLabelUtil;
import com.jforex.dzjforex.order.TradeUtility;
import com.jforex.programming.order.task.params.basic.SubmitParams;

import io.reactivex.Single;

public class OrderSubmit {

    private final TradeUtility tradeUtility;
    private final OrderSubmitParams orderSubmitParams;
    private final OrderLabelUtil orderLabelUtil;

    public OrderSubmit(final TradeUtility tradeUtility,
                       final OrderSubmitParams orderSubmitParams) {
        this.tradeUtility = tradeUtility;
        this.orderSubmitParams = orderSubmitParams;

        orderLabelUtil = tradeUtility.orderLabelUtil();
    }

    public int run(final Instrument instrument,
                   final BrokerBuyData brokerBuyData) {
        final String label = orderLabelUtil.create();
        final Single<SubmitParams> submitParams = orderSubmitParams.get(instrument,
                                                                        brokerBuyData,
                                                                        label);

        return submitParams
            .map(tradeUtility::runTaskParams)
            .map(submitResult -> brokerBuyData.stopDistance() != -1
                    ? evalSubmitResult(submitResult,
                                       brokerBuyData,
                                       label)
                    : ZorroReturnValues.BROKER_BUY_OPPOSITE_CLOSE.getValue())
            .onErrorReturnItem(ZorroReturnValues.BROKER_BUY_FAIL.getValue())
            .blockingGet();
    }

    private int evalSubmitResult(final OrderActionResult submitResult,
                                 final BrokerBuyData brokerBuyData,
                                 final String label) {
        return submitResult == OrderActionResult.FAIL
                ? ZorroReturnValues.BROKER_BUY_FAIL.getValue()
                : fillOpenPriceAndReturnOrderID(brokerBuyData, label);
    }

    private int fillOpenPriceAndReturnOrderID(final BrokerBuyData brokerBuyData,
                                              final String label) {
        final int orderID = orderLabelUtil.idFromLabel(label);
        final IOrder order = tradeUtility
            .maybeOrderByID(orderID)
            .blockingGet();
        brokerBuyData.fillOpenPrice(order.getOpenPrice());

        return orderID;
    }
}
