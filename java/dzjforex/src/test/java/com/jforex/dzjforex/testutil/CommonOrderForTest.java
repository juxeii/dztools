package com.jforex.dzjforex.testutil;
//package com.jforex.dzjforex.test.util;
//
//import static org.hamcrest.Matchers.equalTo;
//import static org.junit.Assert.assertThat;
//
//import java.util.concurrent.TimeUnit;
//
//import org.mockito.Mock;
//
//import com.dukascopy.api.IOrder;
//import com.jforex.dzjforex.history.HistoryProvider;
//import com.jforex.dzjforex.time.ServerTimeProvider;
//import com.jforex.programming.order.OrderUtil;
//import com.jforex.programming.order.event.OrderEvent;
//import com.jforex.programming.order.task.params.ComposeData;
//import com.jforex.programming.order.task.params.RetryParams;
//import com.jforex.programming.rx.RetryDelay;
//import com.jforex.programming.rx.RetryDelayFunction;
//
//import io.reactivex.Observable;
//
//public class CommonOrderForTest extends CommonUtilForTest {
//
//    @Mock
//    protected OrderUtil orderUtilMock;
//    @Mock
//    protected HistoryProvider historyProviderMock;
//    @Mock
//    protected ServerTimeProvider serverTimeProviderMock;
//    @Mock
//    protected IOrder orderMock;
//    protected static final int orderRetries = 3;
//    protected static final long orderRetryDelay = 1500L;
//    protected static final RetryDelay retryDelay = new RetryDelay(orderRetryDelay, TimeUnit.MILLISECONDS);
//    protected static final RetryDelayFunction retryDelayFunction = att -> retryDelay;
//    protected static final RetryParams retryParams = new RetryParams(orderRetries, retryDelayFunction);
//
//    public CommonOrderForTest() {
//        super();
//
//        when(tradeUtilMock.retryParams()).thenReturn(retryParams);
//        when(tradeUtilMock.orderUtil()).thenReturn(orderUtilMock);
//
//        when(orderMock.getLabel()).thenReturn("TestLabel");
//    }
//
//    protected void setOrderUtilObservable(final Observable<OrderEvent> observable) {
//        when(orderUtilMock.paramsToObservable(any())).thenReturn(observable);
//    }
//
//    protected void assertRetryParams(final ComposeData composeData) throws Exception {
//        final RetryParams returnedRetryParams = composeData.retryParams();
//        final RetryDelay returnedRetryDelay = returnedRetryParams
//            .delayFunction()
//            .apply(1);
//
//        assertThat(returnedRetryParams.noOfRetries(), equalTo(orderRetries));
//        assertThat(returnedRetryDelay.delay(), equalTo(orderRetryDelay));
//        assertThat(returnedRetryDelay.timeUnit(), equalTo(TimeUnit.MILLISECONDS));
//
//        // Run handlers for coverage
//        composeData
//            .startAction()
//            .run();
//
//        composeData
//            .completeAction()
//            .run();
//
//        composeData
//            .errorConsumer()
//            .accept(jfException);
//    }
//}
