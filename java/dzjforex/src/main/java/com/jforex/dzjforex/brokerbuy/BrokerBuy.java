package com.jforex.dzjforex.brokerbuy;

import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.order.OrderActionResult;
import com.jforex.dzjforex.order.OrderLabelUtil;
import com.jforex.dzjforex.order.TaskParamsRunner;
import com.jforex.dzjforex.order.TradeUtility;

public class BrokerBuy {

    private final TaskParamsRunner taskParamsRunner;
    private final TradeUtility tradeUtility;
    private final OrderLabelUtil orderLabelUtil;

    public BrokerBuy(final TaskParamsRunner taskParamsRunner,
                     final TradeUtility tradeUtility) {
        this.taskParamsRunner = taskParamsRunner;
        this.tradeUtility = tradeUtility;

        orderLabelUtil = tradeUtility.orderLabelUtil();
    }

    public int openTrade(final BrokerBuyData brokerBuyData) {
        final String orderLabel = orderLabelUtil.create();
        final int orderID = orderLabelUtil
            .idFromLabel(orderLabel)
            .blockingGet();

        return tradeUtility
            .instrumentForTrading(brokerBuyData.instrumentName())
            .map(instrument -> taskParamsRunner.startSubmit(instrument,
                                                            brokerBuyData,
                                                            orderLabel))
            .map(submitResult -> processSubmitResult(submitResult,
                                                     brokerBuyData,
                                                     orderID))
            .onErrorReturnItem(ZorroReturnValues.BROKER_BUY_FAIL.getValue())
            .blockingGet();
    }

    private int processSubmitResult(final OrderActionResult submitResult,
                                    final BrokerBuyData brokerBuyData,
                                    final int orderID) {
        if (brokerBuyData.stopDistance() == -1)
            return ZorroReturnValues.BROKER_BUY_OPPOSITE_CLOSE.getValue();

        return submitResult == OrderActionResult.FAIL
                ? ZorroReturnValues.BROKER_BUY_FAIL.getValue()
                : fillOpenPriceAndReturnOrderID(brokerBuyData, orderID);
    }

    private int fillOpenPriceAndReturnOrderID(final BrokerBuyData brokerBuyData,
                                              final int orderID) {
        return tradeUtility
            .orderByID(orderID)
            .doOnSuccess(order -> brokerBuyData.fillOpenPrice(order.getOpenPrice()))
            .map(order -> orderID)
            .onErrorReturnItem(ZorroReturnValues.BROKER_BUY_FAIL.getValue())
            .blockingGet();
    }
}
