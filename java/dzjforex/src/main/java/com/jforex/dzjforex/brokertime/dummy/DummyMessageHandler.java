package com.jforex.dzjforex.brokertime.dummy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IMessage;
import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public class DummyMessageHandler {

    private final Observable<IMessage> orderMessages;
    private final BehaviorSubject<Boolean> wasOffline = BehaviorSubject.createDefault(false);

    private final static String systemUnavailablePrefix = "SYSTEM_UNAVAILABLE";
    private final static Logger logger = LogManager.getLogger(DummyMessageHandler.class);

    public DummyMessageHandler(final Observable<IMessage> orderMessages) {
        this.orderMessages = orderMessages;
    }

    public boolean wasOffline() {
        return wasOffline.getValue();
    }

    public void handleRejectEvent(final OrderEvent orderEvent) {
        final IOrder order = orderEvent.order();
        orderMessages
            .filter(message -> message.getOrder() != null)
            .filter(msg -> msg.getOrder() == order)
            .takeUntil(msg -> msg.getType() == IMessage.Type.ORDER_SUBMIT_REJECTED)
            .map(IMessage::getContent)
            .subscribe(msgContent -> {
                logger.debug("Received message content for dummy order: " + msgContent);
                if (msgContent.startsWith(systemUnavailablePrefix)) {
                    logger.debug("System unavailable message for dummy order received -> market is closed.");
                    wasOffline.onNext(true);
                } else
                    wasOffline.onNext(false);
            });
    }

    public void handleOKEvent(final OrderEvent orderEvent) {
        logger.debug("Dummy order was opened -> market is open.");
        wasOffline.onNext(false);
        final IOrder order = orderEvent.order();
        Completable
            .fromAction(order::close)
            .subscribe();
    }
}
