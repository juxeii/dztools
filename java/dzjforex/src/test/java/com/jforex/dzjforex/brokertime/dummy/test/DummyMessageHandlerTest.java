package com.jforex.dzjforex.brokertime.dummy.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IMessage;
import com.jforex.dzjforex.brokertime.dummy.DummyMessageHandler;
import com.jforex.dzjforex.testutil.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

@RunWith(HierarchicalContextRunner.class)
public class DummyMessageHandlerTest extends CommonUtilForTest {

    private DummyMessageHandler dummyMessageHandler;

    @Mock
    private IMessage messageMock;
    private final Subject<IMessage> orderMessages = PublishSubject.create();
    private static String systemOfflinePrefix = "SYSTEM_UNAVAILABLE";
    private static String systemOnlinePrefix = "System online";

    @Before
    public void setUp() {
        dummyMessageHandler = new DummyMessageHandler(orderMessages);
    }

    private void assertWasOffline(final boolean expectedWasOffline) {
        assertThat(dummyMessageHandler.wasOffline(), equalTo(expectedWasOffline));
    }

    private void callHandlerOrderEvent() {
        dummyMessageHandler.handleOrderEvent(orderEventA);
        orderMessages.onNext(messageMock);
    }

    @Test
    public void afterCreationMessageWasNotOffline() {
        assertWasOffline(false);
    }

    @Test
    public void whenOrderOfMessageIsNullReturnValueIsNotOffline() {
        when(messageMock.getOrder()).thenReturn(null);

        callHandlerOrderEvent();

        assertWasOffline(false);
    }

    @Test
    public void whenOrderOfMessageDoesNotMatchReturnValueIsNotOffline() {
        when(messageMock.getOrder()).thenReturn(orderMockB);

        callHandlerOrderEvent();

        assertWasOffline(false);
    }

    public class WhenOrderOfMessageMatchesOrderEvent {

        @Before
        public void setUp() {
            when(messageMock.getOrder()).thenReturn(orderMockA);
        }

        @Test
        public void whenTypeOfMessageIsNotSubmitRejectReturnValueIsNotOffline() {
            when(messageMock.getType()).thenReturn(IMessage.Type.NOTIFICATION);

            callHandlerOrderEvent();

            assertWasOffline(false);
        }

        public class WhenTypeOfMessageIsSubmitReject {

            @Before
            public void setUp() {
                when(messageMock.getType()).thenReturn(IMessage.Type.ORDER_SUBMIT_REJECTED);
            }

            @Test
            public void whenContentSignalsSystemOnlineReturnValueIsNotOffline() {
                when(messageMock.getContent()).thenReturn(systemOnlinePrefix);

                callHandlerOrderEvent();

                assertWasOffline(false);
            }

            @Test
            public void whenContentSignalsSystemOfflineReturnValueIsOffline() {
                when(messageMock.getContent()).thenReturn(systemOfflinePrefix);

                callHandlerOrderEvent();

                assertWasOffline(true);
            }
        }
    }
}
