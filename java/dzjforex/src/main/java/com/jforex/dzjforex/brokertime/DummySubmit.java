package com.jforex.dzjforex.brokertime;

import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.misc.DateTimeUtil;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.params.basic.SubmitParams;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;

public class DummySubmit {

    private final OrderUtil orderUtil;
    private final Observable<IMessage> orderMessages;
    private final ServerTimeProvider serverTimeProvider;
    private final BehaviorSubject<Boolean> wasOffline = BehaviorSubject.createDefault(false);

    private static long checkInterval = 10000L;
    private static double invalidSLPrice = 1.12345678;
    private static String systemOfflinePrefix = "System offline";
    private final static Logger logger = LogManager.getLogger(DummySubmit.class);

    public DummySubmit(final OrderUtil orderUtil,
                       final Observable<IMessage> orderMessages,
                       final ServerTimeProvider serverTimeProvider) {
        this.orderUtil = orderUtil;
        this.orderMessages = orderMessages;
        this.serverTimeProvider = serverTimeProvider;

        Observable
            .interval(0L,
                      checkInterval,
                      TimeUnit.MILLISECONDS,
                      Schedulers.io())
            .doOnSubscribe(d -> submit())
            .filter(tick -> isHalfHour())
            .subscribe(tick -> submit());
    }

    private boolean isHalfHour() {
        final long serverTime =
                serverTimeProvider
                    .get()
                    .blockingGet();
        final int minutes = (int) ((serverTime / (1000 * 60)) % 60);
        final boolean isHalfHour = minutes % 2 == 0;
        logger.debug("serverTime " + DateTimeUtil.formatMillis(serverTime)
                + " minutes " + minutes
                + " isHalfHour " + isHalfHour);
        return isHalfHour;
    }

    public boolean wasOffline() {
        return wasOffline.getValue();
    }

    private void submit() {
        final OrderParams orderParams = OrderParams
            .forInstrument(Instrument.EURUSD)
            .withOrderCommand(OrderCommand.BUY)
            .withAmount(0.001)
            .withLabel("DummyOrder")
            .stopLossPrice(invalidSLPrice)
            .build();

        final SubmitParams submitParams = SubmitParams
            .withOrderParams(orderParams)
            .doOnStart(() -> logger.debug("Submitting dummy order now..."))
            .doOnError(e -> logger.error("Submitting dummy order failed! " + e.getMessage()))
            .doOnSubmitReject(this::observeMsgContent)
            .build();

        orderUtil.execute(submitParams);
    }

    private void observeMsgContent(final OrderEvent orderEvent) {
        final IOrder order = orderEvent.order();
        orderMessages
            .doOnNext(msg -> logger.debug("Received msg for order " + msg.getOrder().getLabel()))
            .filter(msg -> msg.getOrder() == order)
            .doOnNext(msg -> logger.debug("Received dummy msg type " + msg.getType()))
            .takeUntil(msg -> msg.getType() == IMessage.Type.ORDER_SUBMIT_REJECTED)
            .map(IMessage::getContent)
            .subscribe(msgContent -> {
                logger.debug("Received msg content for dummy order: " + msgContent);
                if (msgContent.startsWith(systemOfflinePrefix)) {
                    logger.debug("System offline message for dummy order received -> market is closed.");
                    wasOffline.onNext(true);
                } else {
                    logger.debug("Reject message from dummy order received -> market is open.");
                    wasOffline.onNext(false);
                }
            });
    }
}
