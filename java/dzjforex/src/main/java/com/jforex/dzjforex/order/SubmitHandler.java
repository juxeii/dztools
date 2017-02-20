package com.jforex.dzjforex.order;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.ZorroLogger;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.params.basic.SubmitParams;

import io.reactivex.Observable;

public class SubmitHandler {

    private final TradeUtil tradeUtil;

    private final static Logger logger = LogManager.getLogger(SubmitHandler.class);

    public SubmitHandler(final TradeUtil tradeUtil) {
        this.tradeUtil = tradeUtil;
    }

    public IOrder submit(final int orderID,
                         final Instrument instrument,
                         final OrderCommand command,
                         final double amount,
                         final String label,
                         final double slPrice) {
        logger.info("Trying to open trade for " + instrument + "\n"
                + "orderID:  " + orderID + "\n"
                + "command:  " + command + "\n"
                + "amount:  " + amount + "\n"
                + "label:  " + label + "\n"
                + "slPrice: " + slPrice);

        final OrderParams orderParams = OrderParams
            .forInstrument(instrument)
            .withOrderCommand(command)
            .withAmount(amount)
            .withLabel(label)
            .stopLossPrice(slPrice)
            .build();

        final SubmitParams submitParams = SubmitParams
            .withOrderParams(orderParams)
            .doOnComplete(() -> logger.info("Opening trade " + instrument
                    + " with order ID " + orderID + " done."))
            .build();

        final OrderEvent orderEvent = tradeUtil
            .orderUtil()
            .paramsToObservable(submitParams)
            .onErrorResumeNext(err -> {
                ZorroLogger.showError("Failed to open trade with ID "
                        + orderID + "! " + err.getMessage());
                return Observable.just(new OrderEvent(null, OrderEventType.FILL_REJECTED, true));
            })
            .blockingLast();

        return orderEvent.order();
    }
}
