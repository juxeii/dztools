package com.jforex.dzjforex.brokerapi;

import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.ZorroLogger;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.dzjforex.config.ReturnCodes;
import com.jforex.dzjforex.handler.AccountInfo;
import com.jforex.dzjforex.handler.InstrumentHandler;
import com.jforex.dzjforex.handler.OrderHandler;
import com.jforex.programming.instrument.InstrumentUtil;
import com.jforex.programming.math.MathUtil;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.params.basic.SubmitParams;
import com.jforex.programming.strategy.StrategyUtil;

import io.reactivex.Observable;

public class BrokerBuy {

    private final StrategyUtil strategyUtil;
    private final OrderUtil orderUtil;
    private final OrderHandler orderHandler;
    private final AccountInfo accountInfo;
    private final PluginConfig pluginConfig = ConfigFactory.create(PluginConfig.class);

    private final static Logger logger = LogManager.getLogger(BrokerBuy.class);

    public BrokerBuy(final StrategyUtil strategyUtil,
                     final OrderHandler orderHandler,
                     final AccountInfo accountInfo) {
        this.strategyUtil = strategyUtil;
        this.orderHandler = orderHandler;
        this.accountInfo = accountInfo;

        orderUtil = strategyUtil.orderUtil();
    }

    public int handle(final String instrumentName,
                      final double tradeParams[]) {
        return accountInfo.isTradingAllowed()
                ? InstrumentHandler.executeForInstrument(instrumentName,
                                                         instrument -> prepareSubmit(instrument, tradeParams),
                                                         ReturnCodes.BROKER_BUY_FAIL)
                : ReturnCodes.BROKER_BUY_FAIL;
    }

    private int prepareSubmit(final Instrument instrument,
                              final double tradeParams[]) {
        double amount = tradeParams[0];
        final double dStopDist = tradeParams[1];

        OrderCommand cmd = OrderCommand.BUY;
        if (amount < 0) {
            amount = -amount;
            cmd = OrderCommand.SELL;
        }
        // Scale amount to millions
        amount /= pluginConfig.LOT_SCALE();

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
        tradeParams[2] = orderHandler.getOrder(orderID).getOpenPrice();

        return orderID;
    }

    private int submitOrder(final Instrument instrument,
                            final OrderCommand cmd,
                            final double amount,
                            final double SLPrice) {
        final int orderID = orderHandler.createID();
        final String orderLabel = pluginConfig.ORDER_PREFIX_LABEL() + orderID;

        logger.info("Try to open position for " + instrument +
                " with cmd " + cmd + " ,amount " + amount +
                " ,SLPrice " + SLPrice + " ,orderLabel " +
                orderLabel + " orderID " + orderID);
        final OrderParams orderParams = OrderParams
            .forInstrument(instrument)
            .withOrderCommand(cmd)
            .withAmount(amount)
            .withLabel(orderLabel)
            .stopLossPrice(SLPrice)
            .build();

        final SubmitParams submitParams = SubmitParams
            .withOrderParams(orderParams)
            .doOnComplete(() -> logger.info("Order submission for " + instrument +
                    " with cmd " + cmd + " ,amount " + amount +
                    " ,SLPrice " + SLPrice + " ,orderLabel " +
                    orderLabel + " orderID " + orderID + " successful."))
            .build();

        final OrderEvent orderEvent = orderUtil
            .paramsToObservable(submitParams)
            .onErrorResumeNext(err -> {
                ZorroLogger.showError("Failed to open trade! " + err.getMessage());
                return Observable.just(new OrderEvent(null, OrderEventType.FILL_REJECTED, true));
            })
            .blockingLast();

        final IOrder order = orderEvent.order();
        if (order == null)
            return ReturnCodes.BROKER_BUY_FAIL;

        orderHandler.storeOrder(orderID, order);

        return orderID;
    }
}
