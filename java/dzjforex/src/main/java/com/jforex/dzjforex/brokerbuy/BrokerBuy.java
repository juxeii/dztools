package com.jforex.dzjforex.brokerbuy;

import com.dukascopy.api.IOrder;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.order.OrderLabelUtil;
import com.jforex.dzjforex.order.OrderRepository;
import com.jforex.dzjforex.order.TaskParamsRunner;
import com.jforex.dzjforex.order.TradeUtility;

import io.reactivex.Single;

public class BrokerBuy {

    private final TaskParamsRunner taskParamsRunner;
    private final OrderRepository orderRepository;
    private final TradeUtility tradeUtility;
    private final OrderLabelUtil orderLabelUtil;

    public BrokerBuy(final TaskParamsRunner taskParamsRunner,
                     final OrderRepository orderRepository,
                     final TradeUtility tradeUtility) {
        this.taskParamsRunner = taskParamsRunner;
        this.orderRepository = orderRepository;
        this.tradeUtility = tradeUtility;

        orderLabelUtil = tradeUtility.orderLabelUtil();
    }

    public int openTrade(final BrokerBuyData brokerBuyData) {
        return tradeUtility
            .instrumentForTrading(brokerBuyData.instrumentName())
            .flatMap(instrument -> taskParamsRunner.startSubmit(instrument,
                                                                brokerBuyData,
                                                                orderLabelUtil.create()))
            .flatMap(order -> processOrderAndGetResult(order, brokerBuyData))
            .onErrorReturnItem(ZorroReturnValues.BROKER_BUY_FAIL.getValue())
            .blockingGet();
    }

    private Single<Integer> processOrderAndGetResult(final IOrder order,
                                                     final BrokerBuyData brokerBuyData) {
        return orderRepository
            .store(order)
            .doOnComplete(() -> brokerBuyData.fillOpenPrice(order))
            .andThen(orderLabelUtil.idFromOrder(order))
            .toSingle()
            .map(orderID -> brokerBuyData.stopDistance() == -1
                    ? ZorroReturnValues.BROKER_BUY_OPPOSITE_CLOSE.getValue()
                    : orderID);
    }
}
