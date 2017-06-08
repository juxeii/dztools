package com.jforex.dzjforex.brokerbuy;

import com.dukascopy.api.IOrder;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.order.OrderRepository;
import com.jforex.dzjforex.order.TradeUtility;

import io.reactivex.Single;

public class BrokerBuy {

    private final SubmitParamsRunner submitParamsRunner;
    private final OrderRepository orderRepository;
    private final TradeUtility tradeUtility;

    private final static double oppositeClose = -1;

    public BrokerBuy(final SubmitParamsRunner submitParamsRunner,
                     final OrderRepository orderRepository,
                     final TradeUtility tradeUtility) {
        this.submitParamsRunner = submitParamsRunner;
        this.orderRepository = orderRepository;
        this.tradeUtility = tradeUtility;
    }

    public Single<Integer> openTrade(final BrokerBuyData brokerBuyData) {
        return Single
            .defer(() -> tradeUtility.instrumentForTrading(brokerBuyData.assetName()))
            .flatMap(instrument -> submitParamsRunner.get(instrument, brokerBuyData))
            .flatMap(order -> processOrderAndGetResult(order, brokerBuyData))
            .onErrorReturnItem(ZorroReturnValues.BROKER_BUY_FAIL.getValue());
    }

    private Single<Integer> processOrderAndGetResult(final IOrder order,
                                                     final BrokerBuyData brokerBuyData) {
        return orderRepository
            .store(order)
            .doOnComplete(() -> brokerBuyData.fillOpenPrice(order))
            .toSingleDefault(brokerBuyData.slDistance() == oppositeClose
                    ? ZorroReturnValues.BROKER_BUY_OPPOSITE_CLOSE.getValue()
                    : brokerBuyData.orderID());
    }
}
