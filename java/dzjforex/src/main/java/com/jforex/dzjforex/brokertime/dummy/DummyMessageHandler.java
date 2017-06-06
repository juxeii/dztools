package com.jforex.dzjforex.brokertime.dummy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jforex.programming.order.event.OrderEvent;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.subjects.BehaviorSubject;

public class DummyMessageHandler {

    private final BehaviorSubject<Boolean> wasOffline = BehaviorSubject.createDefault(false);

    private final static String systemUnavailablePrefix = "SYSTEM_UNAVAILABLE";
    private final static Logger logger = LogManager.getLogger(DummyMessageHandler.class);

    public boolean wasOffline() {
        return wasOffline.getValue();
    }

    public void handleRejectEvent(final OrderEvent orderEvent) {
        final String messageContent = orderEvent
            .message()
            .getContent();

        if (messageContent.startsWith(systemUnavailablePrefix)) {
            logger.debug("System unavailable message for dummy order received -> market is closed.");
            wasOffline.onNext(true);
        } else
            wasOffline.onNext(false);
    }

    public void handleOKEvent(final OrderEvent orderEvent) {
        Single
            .just(orderEvent.order())
            .doOnSubscribe(d -> {
                wasOffline.onNext(false);
                logger.debug("Dummy order was opened -> market is open.");
            })
            .flatMapCompletable(order -> Completable.fromAction(order::close))
            .subscribe();
    }
}
