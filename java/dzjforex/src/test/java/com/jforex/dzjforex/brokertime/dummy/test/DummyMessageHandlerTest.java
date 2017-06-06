package com.jforex.dzjforex.brokertime.dummy.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.dukascopy.api.JFException;
import com.jforex.dzjforex.brokertime.dummy.DummyMessageHandler;
import com.jforex.dzjforex.testutil.CommonUtilForTest;

public class DummyMessageHandlerTest extends CommonUtilForTest {

    private DummyMessageHandler dummyMessageHandler;

    private static String systemOfflinePrefix = "SYSTEM_UNAVAILABLE";
    private static String systemOnlinePrefix = "System online";

    @Before
    public void setUp() {
        dummyMessageHandler = new DummyMessageHandler();
    }

    private void assertWasOffline(final boolean expectedWasOffline) {
        assertThat(dummyMessageHandler.wasOffline(), equalTo(expectedWasOffline));
    }

    private void callHandlerOrderEvent() {
        dummyMessageHandler.handleRejectEvent(orderEventA);
    }

    @Test
    public void afterCreationMessageWasNotOffline() {
        assertWasOffline(false);
    }

    @Test
    public void handleOKClosesOrder() throws JFException {
        dummyMessageHandler.handleOKEvent(orderEventA);

        verify(orderMockA).close();

        assertWasOffline(false);
    }

    @Test
    public void handleRejectEventForSystemAvailableReturnValueIsNotOffline() {
        when(messageMock.getContent()).thenReturn(systemOnlinePrefix);

        callHandlerOrderEvent();

        assertWasOffline(false);
    }

    @Test
    public void handleRejectEventForSystemUnavailableReturnValueIsOffline() {
        when(messageMock.getContent()).thenReturn(systemOfflinePrefix);

        callHandlerOrderEvent();

        assertWasOffline(true);
    }
}
