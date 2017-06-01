package com.jforex.dzjforex.brokertime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.task.params.basic.SubmitParams;

public class DummySubmitRunner {

    private final OrderUtil orderUtil;
    private final DummyMessageHandler dummyMessageHandler;

    private static String orderLabel = "DummyOrder";
    private static double invalidSLPrice = 1.12345678;
    private static double amount = 0.001;
    private final static Logger logger = LogManager.getLogger(DummySubmitRunner.class);

    public DummySubmitRunner(final OrderUtil orderUtil,
                             final DummyMessageHandler dummyMessageHandler) {
        this.orderUtil = orderUtil;
        this.dummyMessageHandler = dummyMessageHandler;
    }

    public boolean wasOffline() {
        return dummyMessageHandler.wasOffline();
    }

    public void start() {
        final OrderParams orderParams = OrderParams
            .forInstrument(Instrument.EURUSD)
            .withOrderCommand(OrderCommand.BUY)
            .withAmount(amount)
            .withLabel(orderLabel)
            .stopLossPrice(invalidSLPrice)
            .build();

        final SubmitParams submitParams = SubmitParams
            .withOrderParams(orderParams)
            .doOnStart(() -> logger.debug("Submitting dummy order now..."))
            .doOnError(e -> logger.error("Submitting dummy order failed! " + e.getMessage()))
            .doOnSubmitReject(dummyMessageHandler::handleOrderEvent)
            .build();

        orderUtil.execute(submitParams);
    }
}
