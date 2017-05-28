package com.jforex.dzjforex.history.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.stubbing.OngoingStubbing;

import com.dukascopy.api.IBar;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.ITick;
import com.dukascopy.api.JFException;
import com.google.common.collect.Lists;
import com.jforex.dzjforex.history.HistoryWrapper;
import com.jforex.dzjforex.misc.TimeSpan;
import com.jforex.dzjforex.testutil.BarsAndTicksForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class HistoryWrapperTest extends BarsAndTicksForTest {

    private HistoryWrapper historyWrapper;

    @Mock
    private IHistory historyMock;
    private final long startDate = 12;
    private final long endDate = 42;
    private final TimeSpan timeSpan = new TimeSpan(startDate, endDate);

    @Before
    public void setUp() {
        historyWrapper = new HistoryWrapper(historyMock);
    }

    public class GetBarCall {

        private final int shift = 1;

        private OngoingStubbing<IBar> getStub() throws JFException {
            return when(historyMock.getBar(instrumentForTest,
                                           period,
                                           offerSide,
                                           shift));
        }

        private TestObserver<IBar> test() {
            return historyWrapper
                .getBar(barParams, shift)
                .test();
        }

        @Test
        public void barFromHistoryIsReturned() throws JFException {
            getStub().thenReturn(barAMock);

            test()
                .assertComplete()
                .assertValue(barAMock);
        }

        @Test
        public void execpetionOfHistoryIsPropagated() throws JFException {
            getStub().thenThrow(jfException);

            test().assertError(jfException);
        }
    }

    public class GetBarsCall {

        private OngoingStubbing<List<IBar>> getStub() throws JFException {
            return when(historyMock.getBars(instrumentForTest,
                                            period,
                                            offerSide,
                                            startDate,
                                            endDate));
        }

        private TestObserver<List<IBar>> subscribe() {
            return historyWrapper
                .getBarsReversed(barParams, timeSpan)
                .test();
        }

        @Test
        public void barsFromHistoryAreReturnedAndReversed() throws JFException {
            getStub().thenReturn(barMockList);

            final List<IBar> returnedBars = subscribe()
                .values()
                .get(0);

            assertThat(returnedBars.get(0), equalTo(barCMock));
            assertThat(returnedBars.get(1), equalTo(barBMock));
            assertThat(returnedBars.get(2), equalTo(barAMock));
        }

        @Test
        public void execpetionOfHistoryIsPropagated() throws JFException {
            getStub().thenThrow(jfException);

            subscribe().assertError(jfException);
        }
    }

    public class GetTicksCall {

        private OngoingStubbing<List<ITick>> getStub() throws JFException {
            return when(historyMock.getTicks(instrumentForTest,
                                             startDate,
                                             endDate));
        }

        private TestObserver<List<ITick>> subscribe() {
            return historyWrapper
                .getTicksReversed(instrumentForTest, timeSpan)
                .test();
        }

        @Test
        public void ticksFromHistoryAreReturnedAndReversed() throws JFException {
            getStub().thenReturn(tickMockList);

            final List<ITick> returnedTicks = subscribe()
                .values()
                .get(0);

            assertThat(returnedTicks.get(0), equalTo(tickCMock));
            assertThat(returnedTicks.get(1), equalTo(tickBMock));
            assertThat(returnedTicks.get(2), equalTo(tickAMock));
        }

        @Test
        public void execpetionOfHistoryIsPropagated() throws JFException {
            getStub().thenThrow(jfException);

            subscribe().assertError(jfException);
        }
    }

    public class GetTimeOfLastTickCall {

        private final long lastTickTime = 42L;

        private OngoingStubbing<Long> getStub() throws JFException {
            return when(historyMock.getTimeOfLastTick(instrumentForTest));
        }

        private TestObserver<Long> subscribe() {
            return historyWrapper
                .getTimeOfLastTick(instrumentForTest)
                .test();
        }

        @Test
        public void timeFromHistoryIsReturned() throws JFException {
            getStub().thenReturn(lastTickTime);

            subscribe()
                .assertComplete()
                .assertValue(lastTickTime);
        }

        @Test
        public void execpetionOfHistoryIsPropagated() throws JFException {
            getStub().thenThrow(jfException);

            subscribe().assertError(jfException);
        }
    }

    public class GetOrdersHistoryCall {

        private final List<IOrder> orders = Lists.newArrayList();

        private OngoingStubbing<List<IOrder>> getStub() throws JFException {
            return when(historyMock.getOrdersHistory(instrumentForTest,
                                                     startDate,
                                                     endDate));
        }

        private TestObserver<List<IOrder>> subscribe() {
            return historyWrapper
                .getOrdersHistory(instrumentForTest, timeSpan)
                .test();
        }

        @Test
        public void ordersFromHistoryAreReturned() throws JFException {
            getStub().thenReturn(orders);

            subscribe()
                .assertComplete()
                .assertValue(orders);
        }

        @Test
        public void execpetionOfHistoryIsPropagated() throws JFException {
            getStub().thenThrow(jfException);

            subscribe().assertError(jfException);
        }
    }
}
