package com.jforex.dzjforex.order;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.task.params.basic.SubmitParams;

public class OrderSubmit {

    private final TradeUtil tradeUtil;

    private final static Logger logger = LogManager.getLogger(OrderSubmit.class);

    public OrderSubmit(final TradeUtil tradeUtil) {
        this.tradeUtil = tradeUtil;
    }

    public OrderActionResult run(final Instrument instrument,
                                 final OrderCommand command,
                                 final double amount,
                                 final String label,
                                 final double slPrice) {
        final OrderParams orderParams = OrderParams
            .forInstrument(instrument)
            .withOrderCommand(command)
            .withAmount(amount)
            .withLabel(label)
            .stopLossPrice(slPrice)
            .build();

        final SubmitParams submitParams = SubmitParams
            .withOrderParams(orderParams)
            .doOnStart(() -> logger.info("Trying to open trade for " + instrument + "\n"
                    + "command:  " + command + "\n"
                    + "amount:  " + amount + "\n"
                    + "label:  " + label + "\n"
                    + "slPrice: " + slPrice))
            .doOnError(err -> logger.error("Opening trade for " + instrument
                    + " with label  " + label + " failed!" + err.getMessage()))
            .doOnComplete(() -> logger.info("Opening trade for " + instrument
                    + " with label  " + label + " done."))
            .retryOnReject(tradeUtil.retryParams())
            .build();

        return tradeUtil.runTaskParams(submitParams);
    }
}
