package com.jforex.dzjforex.history.test;

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
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.google.common.collect.Lists;
import com.jforex.dzjforex.history.HistoryWrapper;
import com.jforex.dzjforex.test.util.CommonUtilForTest;
import com.jforex.programming.quote.BarParams;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class HistoryWrapperTest extends CommonUtilForTest {

    private HistoryWrapper historyWrapper;

    @Mock
    private IHistory historyMock;
    @Mock
    private IBar barMock;
    private final Period testPeriod = Period.ONE_MIN;
    private final OfferSide testOfferSide = OfferSide.ASK;
    private final long startDate = 12;
    private final long endDate = 42;
    private BarParams barParams;

    @Before
    public void setUp() {
        barParams = BarParams
            .forInstrument(instrumentForTest)
            .period(testPeriod)
            .offerSide(testOfferSide);

        historyWrapper = new HistoryWrapper(historyMock);
    }

    public class GetBarCall {

        private final int shift = 1;

        private OngoingStubbing<IBar> getStub() throws JFException {
            return when(historyMock.getBar(instrumentForTest,
                                           testPeriod,
                                           testOfferSide,
                                           shift));
        }

        private TestObserver<IBar> test() {
            return historyWrapper
                .getBar(barParams, shift)
                .test();
        }

        @Test
        public void BarFromHistoryIsReturned() throws JFException {
            getStub().thenReturn(barMock);

            test()
                .assertComplete()
                .assertValue(barMock);
        }

        @Test
        public void ExecpetionOfHistoryIsPropagated() throws JFException {
            getStub().thenThrow(jfException);

            test().assertError(jfException);
        }
    }

    public class GetBarsCall {

        private final List<IBar> bars = Lists.newArrayList();

        private OngoingStubbing<List<IBar>> getStub() throws JFException {
            return when(historyMock.getBars(instrumentForTest,
                                            testPeriod,
                                            testOfferSide,
                                            startDate,
                                            endDate));
        }

        private TestObserver<List<IBar>> test() {
            return historyWrapper
                .getBars(barParams,
                         startDate,
                         endDate)
                .test();
        }

        @Test
        public void BarsFromHistoryAreReturned() throws JFException {
            getStub().thenReturn(bars);

            test()
                .assertComplete()
                .assertValue(bars);
        }

        @Test
        public void ExecpetionOfHistoryIsPropagated() throws JFException {
            getStub().thenThrow(jfException);

            test().assertError(jfException);
        }
    }

    public class GetTicksCall {

        private final List<ITick> ticks = Lists.newArrayList();

        private OngoingStubbing<List<ITick>> getStub() throws JFException {
            return when(historyMock.getTicks(instrumentForTest,
                                             startDate,
                                             endDate));
        }

        private TestObserver<List<ITick>> test() {
            return historyWrapper
                .getTicks(instrumentForTest,
                          startDate,
                          endDate)
                .test();
        }

        @Test
        public void TicksFromHistoryAreReturned() throws JFException {
            getStub().thenReturn(ticks);

            test()
                .assertComplete()
                .assertValue(ticks);
        }

        @Test
        public void ExecpetionOfHistoryIsPropagated() throws JFException {
            getStub().thenThrow(jfException);

            test().assertError(jfException);
        }
    }

    public class GetTimeOfLastTickCall {

        private final long lastTickTime = 42L;

        private OngoingStubbing<Long> getStub() throws JFException {
            return when(historyMock.getTimeOfLastTick(instrumentForTest));
        }

        private TestObserver<Long> test() {
            return historyWrapper
                .getTimeOfLastTick(instrumentForTest)
                .test();
        }

        @Test
        public void TimeFromHistoryIsReturned() throws JFException {
            getStub().thenReturn(lastTickTime);

            test()
                .assertComplete()
                .assertValue(lastTickTime);
        }

        @Test
        public void ExecpetionOfHistoryIsPropagated() throws JFException {
            getStub().thenThrow(jfException);

            test().assertError(jfException);
        }
    }

    public class GetOrdersHistoryCall {

        private final List<IOrder> orders = Lists.newArrayList();

        private OngoingStubbing<List<IOrder>> getStub() throws JFException {
            return when(historyMock.getOrdersHistory(instrumentForTest,
                                                     startDate,
                                                     endDate));
        }

        private TestObserver<List<IOrder>> test() {
            return historyWrapper
                .getOrdersHistory(instrumentForTest,
                                  startDate,
                                  endDate)
                .test();
        }

        @Test
        public void OrdersFromHistoryAreReturned() throws JFException {
            getStub().thenReturn(orders);

            test()
                .assertComplete()
                .assertValue(orders);
        }

        @Test
        public void ExecpetionOfHistoryIsPropagated() throws JFException {
            getStub().thenThrow(jfException);

            test().assertError(jfException);
        }
    }
}
