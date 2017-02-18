package com.jforex.dzjforex.brokerapi;

import com.dukascopy.api.IOrder;
import com.jforex.dzjforex.ZorroLogger;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.dzjforex.config.Constant;
import com.jforex.dzjforex.handler.AccountInfo;
import com.jforex.dzjforex.handler.OrderHandler;
import com.jforex.programming.order.OrderStaticUtil;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.params.basic.CloseParams;
import com.jforex.programming.strategy.StrategyUtil;

import io.reactivex.Observable;

public class BrokerSell extends BrokerOrderBase {

    private final OrderHandler orderHandler;

    public BrokerSell(final StrategyUtil strategyUtil,
                      final OrderHandler orderHandler,
                      final AccountInfo accountInfo,
                      final PluginConfig pluginConfig) {
        super(strategyUtil,
              accountInfo,
              pluginConfig);

        this.orderHandler = orderHandler;
    }

    public int closeTrade(final int nTradeID,
                          final int nAmount) {
        logger.info("closeTrade called");
        if (!orderHandler.isOrderKnown(nTradeID) || !accountInfo.isTradingAllowed()) {
            logger.info("Close trade not possible");
            return Constant.UNKNOWN_ORDER_ID;
        }

        final double convertedAmount = Math.abs(nAmount) / pluginConfig.LOT_SCALE();
        logger.info("nTradeID " + nTradeID
                + " amount: " + nAmount
                + " convertedAmount " + convertedAmount);

        return closeOrder(nTradeID, convertedAmount);
    }

    private int closeOrder(final int nTradeID,
                           final double convertedAmount) {
        logger.info("closeOrder called");
        final IOrder order = orderHandler.getOrder(nTradeID);
        if (order.getState() != IOrder.State.OPENED && order.getState() != IOrder.State.FILLED) {
            logger.warn("Order " + nTradeID + " could not be closed. Order state: " + order.getState());
            return Constant.BROKER_SELL_FAIL;
        }

        final CloseParams closeParams = CloseParams
            .withOrder(order)
            .closePartial(convertedAmount)
            .build();

        final OrderEvent orderEvent = orderUtil
            .paramsToObservable(closeParams)
            .onErrorResumeNext(err -> {
                ZorroLogger.showError("Failed to close trade! " + err.getMessage());
                return Observable.just(new OrderEvent(null, OrderEventType.CLOSE_REJECTED, true));
            })
            .blockingLast();

        if (orderEvent.order() == null) {
            logger.info("closeOrder failed");
            return Constant.BROKER_SELL_FAIL;
        } else if (!OrderStaticUtil.isClosed.test(order)) {
            final int newOrderID = orderHandler.createID();
            orderHandler.storeOrder(newOrderID, order);
            logger.info("order only partially closed id " + newOrderID);
            return newOrderID;
        } else {
            logger.info("order closed completely id " + nTradeID);
            return nTradeID;
        }
    }
}
