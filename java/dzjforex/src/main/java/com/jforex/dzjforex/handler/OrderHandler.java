package com.jforex.dzjforex.handler;

import java.rmi.server.UID;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IContext;
import com.dukascopy.api.IEngine;
import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.jforex.dzjforex.ZorroLogger;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.dzjforex.config.ReturnCodes;
import com.jforex.dzjforex.misc.InstrumentProvider;
import com.jforex.dzjforex.ordertask.CloseOrderTask;
import com.jforex.dzjforex.ordertask.StopLossTask;
import com.jforex.dzjforex.ordertask.SubmitOrderTask;
import com.jforex.programming.instrument.InstrumentUtil;
import com.jforex.programming.math.MathUtil;
import com.jforex.programming.strategy.StrategyUtil;

public class OrderHandler {

    private final IContext context;
    private final IEngine engine;
    private final StrategyUtil strategyUtil;
    private final HashMap<Integer, IOrder> orderMap;
    private final PluginConfig pluginConfig;

    private final static Logger logger = LogManager.getLogger(OrderHandler.class);

    public OrderHandler(final IContext context,
                        final StrategyUtil strategyUtil,
                        final PluginConfig pluginConfig) {
        this.context = context;
        this.strategyUtil = strategyUtil;
        this.pluginConfig = pluginConfig;
        this.engine = context.getEngine();

        orderMap = new HashMap<Integer, IOrder>();
        resumeOrderIDs();
    }

    public int doBrokerBuy(final String instrumentName,
                           final double tradeParams[]) {
        final Optional<Instrument> instrumentOpt = InstrumentProvider.fromName(instrumentName);
        if (!instrumentOpt.isPresent())
            return ReturnCodes.BROKER_BUY_FAIL;
        double amount = tradeParams[0];
        final double dStopDist = tradeParams[1];

        OrderCommand cmd = OrderCommand.BUY;
        if (amount < 0) {
            amount = -amount;
            cmd = OrderCommand.SELL;
        }
        // Scale amount to millions
        amount /= pluginConfig.LOT_SCALE();

        final Instrument instrument = instrumentOpt.get();
        final InstrumentUtil instrumentUtil = strategyUtil.instrumentUtil(instrument);
        final double currentAskPrice = instrumentUtil.askQuote();
        final double spread = instrumentUtil.spread();

        double SLPrice = 0;
        if (dStopDist > 0) {
            if (cmd == OrderCommand.BUY)
                SLPrice = currentAskPrice - dStopDist - spread;
            else
                SLPrice = currentAskPrice + dStopDist;
        }
        final int orderID = submitOrder(instrument, cmd, amount, MathUtil.roundPrice(SLPrice, instrument));
        if (orderID == ReturnCodes.UNKNOWN_ORDER_ID) {
            logger.warn("Could not open position for " + instrument);
            ZorroLogger.log("Could not open position for " + instrument);
            return ReturnCodes.BROKER_BUY_FAIL;
        }
        tradeParams[2] = orderMap.get(orderID).getOpenPrice();

        return orderID;
    }

    public int doBrokerTrade(final int orderID,
                             final double orderParams[]) {
        if (!isOrderIDValid(orderID)) {
            logger.warn("Order ID " + orderID + " is unknown!");
            ZorroLogger.log("Order ID " + orderID + " is unknown!");
            return ReturnCodes.UNKNOWN_ORDER_ID;
        }

        final IOrder order = orderMap.get(orderID);
        final InstrumentUtil instrumentUtil = strategyUtil.instrumentUtil(order.getInstrument());
        orderParams[0] = order.getOpenPrice();

        if (order.isLong())
            orderParams[1] = instrumentUtil.askQuote();
        else
            orderParams[1] = instrumentUtil.bidQuote();
        // Rollover not supported by Dukascopy
        orderParams[2] = 0.0;
        orderParams[3] = order.getProfitLossInAccountCurrency();
        final int orderAmount = (int) (order.getAmount() * pluginConfig.LOT_SCALE());

        return order.getState() == IOrder.State.CLOSED
                ? -orderAmount
                : orderAmount;
    }

    public int doBrokerStop(final int orderID,
                            final double newSLPrice) {
        logger.debug("orderID " + orderID + " newSLPrice: " + newSLPrice);

        if (!isOrderIDValid(orderID)) {
            logger.warn("Order ID " + orderID + " is unknown!");
            ZorroLogger.log("Order ID " + orderID + " is unknown!");
            return ReturnCodes.UNKNOWN_ORDER_ID;
        }

        final IOrder order = orderMap.get(orderID);
        if (order.getStopLossPrice() == 0) {
            logger.warn("Order has no SL set -> reject BrokerStop!");
            ZorroLogger.log("Order has no SL set -> reject BrokerStop!");
            return ReturnCodes.UNKNOWN_ORDER_ID;
        }
        return setSLPrice(order, MathUtil.roundPrice(newSLPrice, order.getInstrument()));
    }

    public int doBrokerSell(final int orderID,
                            final int amount) {
        if (!orderMap.containsKey(orderID))
            return ReturnCodes.UNKNOWN_ORDER_ID;

        final double convertedAmount = Math.abs(amount) / pluginConfig.LOT_SCALE();
        logger.debug("orderID " + orderID + " amount: " + amount + " convertedAmount " + convertedAmount);

        return closeOrder(orderID, convertedAmount);
    }

    public synchronized int submitOrder(final Instrument instrument,
                                        final OrderCommand cmd,
                                        final double amount,
                                        final double SLPrice) {
        final int orderID = Math.abs(new UID().hashCode());
        final String orderLabel = pluginConfig.ORDER_PREFIX_LABEL() + orderID;

        logger.info("Try to open position for " + instrument +
                " with cmd " + cmd + " ,amount " + amount +
                " ,SLPrice " + SLPrice + " ,orderLabel " +
                orderLabel + " orderID " + orderID);
        final SubmitOrderTask task = new SubmitOrderTask(engine, orderLabel, instrument, cmd, amount, SLPrice);
        final IOrder order = getOrderFromFuture(context.executeTask(task));
        if (order == null)
            return ReturnCodes.BROKER_BUY_FAIL;
        logger.info("Order submission for " + instrument +
                " with cmd " + cmd + " ,amount " + amount +
                " ,SLPrice " + SLPrice + " ,orderLabel " +
                orderLabel + " orderID " + orderID + " successful.");
        orderMap.put(orderID, order);

        return orderID;
    }

    public boolean isOrderIDValid(final int orderID) {
        return orderMap.containsKey(orderID);
    }

    public int closeOrder(final int orderID,
                          final double amount) {
        IOrder order = orderMap.get(orderID);
        if (order.getState() != IOrder.State.OPENED && order.getState() != IOrder.State.FILLED) {
            logger.warn("Order " + orderID + " could not be closed. Order state: " + order.getState());
            return ReturnCodes.BROKER_SELL_FAIL;
        }

        final CloseOrderTask task = new CloseOrderTask(order, amount);
        order = getOrderFromFuture(context.executeTask(task));
        return order.getState() != IOrder.State.CLOSED
                ? ReturnCodes.BROKER_SELL_FAIL
                : orderID;
    }

    public int setSLPrice(IOrder order,
                          final double newSLPrice) {
        final StopLossTask task = new StopLossTask(order, newSLPrice);
        order = getOrderFromFuture(context.executeTask(task));

        return ReturnCodes.ADJUST_SL_OK;
    }

    private IOrder getOrderFromFuture(final Future<IOrder> orderFuture) {
        try {
            return orderFuture.get();
        } catch (final InterruptedException e) {
            logger.error("InterruptedException: " + e.getMessage());
        } catch (final ExecutionException e) {
            logger.error("ExecutionException: " + e.getMessage());
        }
        ZorroLogger.indicateError();
        return null;
    }

    private synchronized void resumeOrderIDs() {
        List<IOrder> orders = null;
        try {
            orders = engine.getOrders();
        } catch (final JFException e) {
            logger.error("getOrders exc: " + e.getMessage());
            ZorroLogger.indicateError();
        }
        for (final IOrder order : orders)
            resumeOrderIDIfFound(order);
    }

    private void resumeOrderIDIfFound(final IOrder order) {
        final String label = order.getLabel();
        if (label.startsWith(pluginConfig.ORDER_PREFIX_LABEL())) {
            final int id = getOrderIDFromLabel(label);
            orderMap.put(id, order);
        }
    }

    private int getOrderIDFromLabel(final String label) {
        final String idName = label.substring(pluginConfig.ORDER_PREFIX_LABEL().length());
        return Integer.parseInt(idName);
    }
}
