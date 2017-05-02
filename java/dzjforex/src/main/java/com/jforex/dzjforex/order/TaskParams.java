package com.jforex.dzjforex.order;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.task.params.RetryParams;
import com.jforex.programming.order.task.params.basic.CloseParams;
import com.jforex.programming.order.task.params.basic.SetSLParams;
import com.jforex.programming.order.task.params.basic.SubmitParams;

public class TaskParams {

    private final RetryParams retryParams;

    private final static Logger logger = LogManager.getLogger(TaskParams.class);

    public TaskParams(final RetryParams retryParams) {
        this.retryParams = retryParams;
    }

    public SubmitParams forSubmit(final Instrument instrument,
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

        return SubmitParams
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
            .retryOnReject(retryParams)
            .build();
    }

    public CloseParams forClose(final IOrder order,
                                final double closeAmount) {
        return CloseParams
            .withOrder(order)
            .closePartial(closeAmount)
            .doOnStart(() -> logger.info("Trying to close order " + order.getLabel()
                    + " with amount " + closeAmount))
            .doOnError(err -> logger.error("Failed to close order "
                    + order.getLabel() + "! " + err.getMessage()))
            .doOnComplete(() -> logger.info("Closing order " + order.getLabel() + " done."))
            .retryOnReject(retryParams)
            .build();
    }

    public SetSLParams forSetSL(final IOrder order,
                                final double newSLPrice) {
        return SetSLParams
            .setSLAtPrice(order, newSLPrice)
            .doOnStart(() -> logger.info("Trying to set new stop loss " + newSLPrice
                    + " on order " + order.getLabel()))
            .doOnError(err -> logger.error("Failed to set new stop loss " + newSLPrice
                    + " on order " + order.getLabel() + "!" + err.getMessage()))
            .doOnComplete(() -> logger.info("Setting new Stop loss " + newSLPrice
                    + " on order " + order.getLabel() + " done."))
            .retryOnReject(retryParams)
            .build();
    }
}
