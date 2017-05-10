package com.jforex.dzjforex.brokerbuy;

import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.order.OrderActionResult;
import com.jforex.dzjforex.order.OrderLabelUtil;
import com.jforex.dzjforex.order.TradeUtility;

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

    public Single<Integer> run(final Instrument instrument,
                               final BrokerBuyData brokerBuyData) {
        final String label = orderLabelUtil.create();

        return orderSubmitParams
            .get(instrument,
                 brokerBuyData,
                 label)
            .map(tradeUtility::runTaskParams)
            .map(submitResult -> evalSubmitResult(submitResult,
                                                  brokerBuyData,
                                                  label));
    }

    private int evalSubmitResult(final OrderActionResult submitResult,
                                 final BrokerBuyData brokerBuyData,
                                 final String label) {
        if (brokerBuyData.stopDistance() == -1)
            return ZorroReturnValues.BROKER_BUY_OPPOSITE_CLOSE.getValue();

        return submitResult == OrderActionResult.FAIL
                ? ZorroReturnValues.BROKER_BUY_FAIL.getValue()
                : fillOpenPriceAndReturnOrderID(brokerBuyData, label);
    }

    private int fillOpenPriceAndReturnOrderID(final BrokerBuyData brokerBuyData,
                                              final String label) {
        return orderLabelUtil
            .idFromLabel(label)
            .flatMap(orderId -> tradeUtility
                .maybeOrderByID(orderId)
                .doOnSuccess(order -> brokerBuyData.fillOpenPrice(order.getOpenPrice()))
                .map(order -> orderId)
                .defaultIfEmpty(ZorroReturnValues.BROKER_BUY_FAIL.getValue()))
            .blockingGet();
    }
}
