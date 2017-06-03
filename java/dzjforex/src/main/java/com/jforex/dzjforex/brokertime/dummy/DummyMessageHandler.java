package com.jforex.dzjforex.brokertime.dummy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IMessage;
import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public class DummyMessageHandler {

    private final Observable<IMessage> orderMessages;
    private final BehaviorSubject<Boolean> wasOffline = BehaviorSubject.createDefault(false);

    private final static String systemOfflinePrefix = "SYSTEM_UNAVAILABLE";
    private final static Logger logger = LogManager.getLogger(DummyMessageHandler.class);

    public DummyMessageHandler(final Observable<IMessage> orderMessages) {
        this.orderMessages = orderMessages;
    }

    public boolean wasOffline() {
        return wasOffline.getValue();
    }

    public void handleOrderEvent(final OrderEvent orderEvent) {
        final IOrder order = orderEvent.order();
        orderMessages
            .filter(message -> message.getOrder() != null)
            .filter(msg -> msg.getOrder() == order)
            .takeUntil(msg -> msg.getType() == IMessage.Type.ORDER_SUBMIT_REJECTED)
            .map(IMessage::getContent)
            .subscribe(msgContent -> {
                logger.debug("Received message content for dummy order: " + msgContent);
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
