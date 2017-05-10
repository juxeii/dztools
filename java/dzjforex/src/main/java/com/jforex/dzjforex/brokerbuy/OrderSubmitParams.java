package com.jforex.dzjforex.brokerbuy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.order.StopLoss;
import com.jforex.dzjforex.order.TradeUtility;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.task.params.RetryParams;
import com.jforex.programming.order.task.params.basic.SubmitParams;
import com.jforex.programming.strategy.StrategyUtil;

import io.reactivex.Single;

public class OrderSubmitParams {

    private final TradeUtility tradeUtility;
    private final StopLoss stopLoss;
    private final RetryParams retryParams;

    private final static Logger logger = LogManager.getLogger(OrderSubmitParams.class);

    public OrderSubmitParams(final TradeUtility tradeUtility,
                             final StopLoss stopLoss,
                             final RetryParams retryParams) {
        this.tradeUtility = tradeUtility;
        this.stopLoss = stopLoss;
        this.retryParams = retryParams;
    }

    public Single<SubmitParams> get(final Instrument instrument,
                                    final BrokerBuyData brokerBuyData,
                                    final String orderLabel) {
        return Single.defer(() -> {
            final double contracts = brokerBuyData.contracts();
            final OrderCommand orderCommand = orderCommandForContracts(contracts);
            final double amount = tradeUtility.contractsToAmount(contracts);

            return stopLoss
                .forDistance(instrument,
                             orderCommand,
                             brokerBuyData.stopDistance())
                .defaultIfEmpty(StrategyUtil.platformSettings.noSLPrice())
                .toSingle()
                .map(slPrice -> create(instrument,
                                       orderCommand,
                                       amount,
                                       orderLabel,
                                       slPrice));
        });
    }

    private SubmitParams create(final Instrument instrument,
                                final OrderCommand orderCommand,
                                final double amount,
                                final String orderLabel,
                                final double slPrice) {

        final OrderParams orderParams = OrderParams
            .forInstrument(instrument)
            .withOrderCommand(orderCommand)
            .withAmount(amount)
            .withLabel(orderLabel)
            .stopLossPrice(slPrice)
            .build();

        return SubmitParams
            .withOrderParams(orderParams)
            .doOnStart(() -> logger.info("Trying to open trade for " + instrument + ":\n"
                    + "command: " + orderCommand + "\n"
                    + "amount: " + amount + "\n"
                    + "label: " + orderLabel + "\n"
                    + "slPrice: " + slPrice))
            .doOnError(e -> logger.error("Opening trade for " + instrument
                    + " with label " + orderLabel
                    + " failed!" + e.getMessage()))
            .doOnComplete(() -> logger.info("Opening trade for " + instrument
                    + " with label " + orderLabel
                    + " done."))
            .retryOnReject(retryParams)
            .build();
    }

    private OrderCommand orderCommandForContracts(final double contracts) {
        return contracts > 0
                ? OrderCommand.BUY
                : OrderCommand.SELL;
    }
}
