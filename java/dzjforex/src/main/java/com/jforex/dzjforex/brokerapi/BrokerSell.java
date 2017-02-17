package com.jforex.dzjforex.brokerapi;

import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.jforex.dzjforex.ZorroLogger;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.dzjforex.config.ReturnCodes;
import com.jforex.dzjforex.handler.AccountInfo;
import com.jforex.dzjforex.handler.OrderHandler;
import com.jforex.programming.order.OrderStaticUtil;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.params.basic.CloseParams;
import com.jforex.programming.strategy.StrategyUtil;

import io.reactivex.Observable;

public class BrokerSell {

    private final OrderUtil orderUtil;
    private final OrderHandler orderHandler;
    private final AccountInfo accountInfo;
    private final PluginConfig pluginConfig = ConfigFactory.create(PluginConfig.class);

    private final static Logger logger = LogManager.getLogger(BrokerBuy.class);

    public BrokerSell(final StrategyUtil strategyUtil,
                      final OrderHandler orderHandler,
                      final AccountInfo accountInfo) {
        this.orderHandler = orderHandler;
        this.accountInfo = accountInfo;

        orderUtil = strategyUtil.orderUtil();
    }

    public int handle(final int nTradeID,
                      final int nAmount) {
        if (!orderHandler.isOrderKnown(nTradeID) || !accountInfo.isTradingAllowed())
            return ReturnCodes.UNKNOWN_ORDER_ID;

        final double convertedAmount = Math.abs(nAmount) / pluginConfig.LOT_SCALE();
        logger.debug("nTradeID " + nTradeID
                + " amount: " + nAmount
                + " convertedAmount " + convertedAmount);

        return closeOrder(nTradeID, convertedAmount);
    }

    public int closeOrder(final int nTradeID,
                          final double convertedAmount) {
        final IOrder order = orderHandler.getOrder(nTradeID);
        if (order.getState() != IOrder.State.OPENED && order.getState() != IOrder.State.FILLED) {
            logger.warn("Order " + nTradeID + " could not be closed. Order state: " + order.getState());
            return ReturnCodes.BROKER_SELL_FAIL;
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
            return ReturnCodes.BROKER_SELL_FAIL;
        } else if (!OrderStaticUtil.isClosed.test(order)) {
            final int newOrderID = orderHandler.createID();
            orderHandler.storeOrder(newOrderID, order);
            return newOrderID;
        } else
            return nTradeID;
    }
}
