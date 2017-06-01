package com.jforex.dzjforex.brokertime;

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

    private static String systemOfflinePrefix = "System offline";
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
            .doOnNext(msg -> logger.debug("Received msg for order " + msg.getOrder().getLabel()))
            .filter(msg -> msg.getOrder() == order)
            .doOnNext(msg -> logger.debug("Received dummy msg type " + msg.getType()))
            .takeUntil(msg -> msg.getType() == IMessage.Type.ORDER_SUBMIT_REJECTED)
            .doOnNext(msg -> logger.debug("Rejected type recieved"))
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
