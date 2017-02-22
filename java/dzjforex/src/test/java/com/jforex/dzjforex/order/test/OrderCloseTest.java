package com.jforex.dzjforex.order.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;

import com.jforex.dzjforex.order.OrderClose;
import com.jforex.dzjforex.order.OrderCloseResult;
import com.jforex.dzjforex.test.util.CommonOrderForTest;
import com.jforex.programming.order.task.params.basic.CloseParams;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;

@RunWith(HierarchicalContextRunner.class)
public class OrderCloseTest extends CommonOrderForTest {

    private OrderClose orderClose;

    @Captor
    private ArgumentCaptor<CloseParams> paramsCaptor;
    private OrderCloseResult result;
    private final double orderAmount = 0.24;
    private final double closeAmount = 0.12;

    @Before
    public void setUp() {
        when(orderMock.getAmount()).thenReturn(orderAmount);

        orderClose = new OrderClose(tradeUtilMock);
    }

    @Test
    public void resultIsFAILWhenObservableFails() {
        setOrderUtilObservable(Observable.error(jfException));

        final OrderCloseResult result = orderClose.run(orderMock, closeAmount);

        assertThat(result, equalTo(OrderCloseResult.FAIL));
    }

    public class WhenCloseCompletes {

        @Before
        public void setUp() {
            setOrderUtilObservable(Observable.empty());
        }

        public class OnFullClose {

            @Before
            public void setUp() {
                result = orderClose.run(orderMock, orderAmount);
            }

            @Test
            public void resultIsOK() {
                assertThat(result, equalTo(OrderCloseResult.OK));
            }

            @Test
            public void closeParamsAreCorrect() throws Exception {
                verify(orderUtilMock).paramsToObservable(paramsCaptor.capture());
                final CloseParams closeParams = paramsCaptor.getValue();

                assertThat(closeParams.order(), equalTo(orderMock));
                assertThat(closeParams.partialCloseAmount(), equalTo(orderAmount));
                assertRetryParams(closeParams.composeData());
            }
        }

        public class OnPartialClose {

            @Before
            public void setUp() {
                result = orderClose.run(orderMock, closeAmount);
            }

            @Test
            public void resultIsOK() {
                assertThat(result, equalTo(OrderCloseResult.PARTIAL_OK));
            }

            @Test
            public void closeParamsAreCorrect() throws Exception {
                verify(orderUtilMock).paramsToObservable(paramsCaptor.capture());
                final CloseParams closeParams = paramsCaptor.getValue();

                assertThat(closeParams.order(), equalTo(orderMock));
                assertThat(closeParams.partialCloseAmount(), equalTo(closeAmount));
                assertRetryParams(closeParams.composeData());
            }
        }
    }
}
