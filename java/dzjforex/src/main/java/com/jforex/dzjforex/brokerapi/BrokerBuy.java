package com.jforex.dzjforex.brokerapi;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.ZorroLogger;
import com.jforex.dzjforex.config.Constant;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.dzjforex.handler.AccountInfo;
import com.jforex.dzjforex.handler.InstrumentHandler;
import com.jforex.dzjforex.handler.OrderHandler;
import com.jforex.programming.instrument.InstrumentUtil;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.params.basic.SubmitParams;
import com.jforex.programming.strategy.StrategyUtil;

import io.reactivex.Observable;

public class BrokerBuy extends BrokerOrderBase {

    private final OrderHandler orderHandler;

    public BrokerBuy(final StrategyUtil strategyUtil,
                     final OrderHandler orderHandler,
                     final AccountInfo accountInfo,
                     final PluginConfig pluginConfig) {
        super(strategyUtil,
              accountInfo,
              pluginConfig);

        this.orderHandler = orderHandler;
    }

    public int doBrokerBuy(final String assetName,
                           final double tradeParams[]) {
        final Instrument instrument = InstrumentHandler
            .fromName(assetName)
            .get();
        final double amount = createTradeAmount(tradeParams[0]);
        final OrderCommand orderCommand = commandForNoOfContracts(tradeParams[0]);
        final double dStopDist = tradeParams[1];
        final double slPrice = calculateSLPrice(instrument,
                                                orderCommand,
                                                dStopDist);
        return submitOrder(instrument,
                           orderCommand,
                           amount,
                           slPrice,
                           tradeParams);
    }

    private double calculateSLPrice(final Instrument instrument,
                                    final OrderCommand orderCommand,
                                    final double dStopDist) {
        if (dStopDist == 0.0 || dStopDist == -1)
            return StrategyUtil.platformSettings.noSLPrice();

        final InstrumentUtil instrumentUtil = strategyUtil.instrumentUtil(instrument);
        final double currentAskPrice = instrumentUtil.askQuote();
        final double spread = instrumentUtil.spread();

        logger.info("currentAskPrice is " + currentAskPrice
                + " spread " + spread + " dStopDist " + dStopDist);

        return orderCommand == OrderCommand.BUY
                ? currentAskPrice - dStopDist - spread
                : currentAskPrice + dStopDist + spread;
    }

    private OrderCommand commandForNoOfContracts(final double noOfContracts) {
        return noOfContracts > 0
                ? OrderCommand.BUY
                : OrderCommand.SELL;
    }

    private double createTradeAmount(final double noOfContracts) {
        return Math.abs(noOfContracts) / pluginConfig.lotScale();
    }

    private int submitOrder(final Instrument instrument,
                            final OrderCommand cmd,
                            final double amount,
                            final double SLPrice,
                            final double tradeParams[]) {
        final int orderID = orderHandler.createID();
        final String orderLabel = pluginConfig.orderLabelPrefix() + orderID;

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
            .doOnComplete(() -> logger.info("Broker buy for " + instrument +
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
            return Constant.BROKER_BUY_FAIL;

        orderHandler.storeOrder(orderID, order);
        tradeParams[2] = orderHandler.getOrder(orderID).getOpenPrice();

        return orderID;
    }
}
