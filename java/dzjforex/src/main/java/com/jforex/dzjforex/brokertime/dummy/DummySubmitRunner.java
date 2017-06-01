package com.jforex.dzjforex.brokertime.dummy;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.task.params.basic.SubmitParams;

public class DummySubmitRunner {

    private final OrderUtil orderUtil;
    private final DummyMessageHandler dummyMessageHandler;

    private final static String orderLabel = "DummyOrder";
    private final static double invalidSLPrice = 1.12345678;
    private final static double amount = 0.001;

    public DummySubmitRunner(final OrderUtil orderUtil,
                             final DummyMessageHandler dummyMessageHandler) {
        this.orderUtil = orderUtil;
        this.dummyMessageHandler = dummyMessageHandler;
    }

    public boolean wasOffline() {
        return dummyMessageHandler.wasOffline();
    }

    public void start() {
        orderUtil.execute(submitParams());
    }

    public SubmitParams submitParams() {
        return SubmitParams
            .withOrderParams(orderParams())
            .doOnSubmitReject(dummyMessageHandler::handleOrderEvent)
            .build();
    }

    public OrderParams orderParams() {
        return OrderParams
            .forInstrument(Instrument.EURUSD)
            .withOrderCommand(OrderCommand.BUY)
            .withAmount(amount)
            .withLabel(orderLabel)
            .stopLossPrice(invalidSLPrice)
            .build();
    }
}
