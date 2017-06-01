package com.jforex.dzjforex.brokertime.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.dukascopy.api.IEngine.OrderCommand;
import com.jforex.dzjforex.brokertime.DummyMessageHandler;
import com.jforex.dzjforex.brokertime.DummySubmitRunner;
import com.jforex.dzjforex.testutil.CommonUtilForTest;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.params.basic.SubmitParams;

import de.bechte.junit.runners.context.HierarchicalContextRunner;

@RunWith(HierarchicalContextRunner.class)
public class DummySubmitRunnerTest extends CommonUtilForTest {

    private DummySubmitRunner dummySubmitRunner;

    @Mock
    private DummyMessageHandler dummyMessageHandlerMock;
    @Captor
    private ArgumentCaptor<SubmitParams> submitParamsCaptor;
    private final String orderLabel = "DummyOrder";
    private final double invalidSLPrice = 1.12345678;
    private final double amount = 0.001;
    private final OrderCommand orderCommand = OrderCommand.BUY;

    @Before
    public void setUp() {
        dummySubmitRunner = new DummySubmitRunner(orderUtilMock, dummyMessageHandlerMock);
    }

    @Test
    public void wasOfflineCallRoutesToMessageHandler() {
        when(dummyMessageHandlerMock.wasOffline()).thenReturn(true);
        assertTrue(dummySubmitRunner.wasOffline());

        when(dummyMessageHandlerMock.wasOffline()).thenReturn(false);
        assertFalse(dummySubmitRunner.wasOffline());

        verify(dummyMessageHandlerMock, times(2)).wasOffline();
    }

    @Test
    public void submitParamsAreCorrect() {
        dummySubmitRunner.start();

        verify(orderUtilMock).execute(submitParamsCaptor.capture());
        final SubmitParams submitParams = submitParamsCaptor.getValue();
        final OrderParams orderParams = submitParams.orderParams();

        assertThat(orderParams.instrument(), equalTo(instrumentForTest));
        assertThat(orderParams.orderCommand(), equalTo(orderCommand));
        assertThat(orderParams.amount(), equalTo(amount));
        assertThat(orderParams.label(), equalTo(orderLabel));
        assertThat(orderParams.stopLossPrice(), equalTo(invalidSLPrice));

        final Consumer<OrderEvent> orderEventConsumer = submitParams
            .composeData()
            .consumerByEventType()
            .get(OrderEventType.SUBMIT_REJECTED);
        orderEventConsumer.accept(orderEventA);
        verify(dummyMessageHandlerMock).handleOrderEvent(orderEventA);
    }
}
