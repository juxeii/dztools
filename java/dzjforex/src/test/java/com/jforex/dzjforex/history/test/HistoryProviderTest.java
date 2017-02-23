package com.jforex.dzjforex.history.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.stubbing.OngoingStubbing;

import com.dukascopy.api.IBar;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.jforex.dzjforex.history.HistoryProvider;
import com.jforex.dzjforex.test.util.CommonUtilForTest;
import com.jforex.dzjforex.test.util.RxTestUtil;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class HistoryProviderTest extends CommonUtilForTest {

    private HistoryProvider historyProvider;

    @Mock
    private IHistory historyMock;
    @Mock
    private IBar barMock;
    @Mock
    private ITick tickMock;
    @Mock
    private IOrder orderMock;
    private final Instrument fetchInstrument = Instrument.EURUSD;
    private final Period period = Period.ONE_HOUR;
    private final long startTime = 12L;
    private final long endTime = 42L;
    private final int maxRetries = 1;
    private final long retryDelay = 1500L;

    @Before
    public void setUp() {
        when(pluginConfigMock.historyDownloadRetries()).thenReturn(maxRetries);
        when(pluginConfigMock.historyRetryDelay()).thenReturn(retryDelay);

        historyProvider = new HistoryProvider(historyMock, pluginConfigMock);
    }

    private void advanceRetryTime(final int times) {
        RxTestUtil.advanceTimeBy(retryDelay * times, TimeUnit.MILLISECONDS);
    }

    public class FetchBars {

        private final OfferSide offerSide = OfferSide.ASK;
        private final List<IBar> barsFromHistory = new ArrayList<>();
        private TestObserver<List<IBar>> testSubscriber;

        @Before
        public void setUp() {
            barsFromHistory.add(barMock);
        }

        private OngoingStubbing<List<IBar>> getBarsStub() throws JFException {
            return when(historyMock.getBars(fetchInstrument,
                                            period,
                                            offerSide,
                                            startTime,
                                            endTime));
        }

        private void verifyGetBarsCall(final int times) throws JFException {
            verify(historyMock, times(times)).getBars(fetchInstrument,
                                                      period,
                                                      offerSide,
                                                      startTime,
                                                      endTime);
        }

        private void subscribeFetch() {
            testSubscriber = historyProvider
                .fetchBars(fetchInstrument,
                           period,
                           offerSide,
                           startTime,
                           endTime)
                .test();
        }

        @Test
        public void whenFetchIsOK() throws JFException {
            getBarsStub().thenReturn(barsFromHistory);

            subscribeFetch();

            testSubscriber
                .assertComplete()
                .assertValue(barsFromHistory);
        }

        @Test
        public void whenFetchFailsTwoTimesAnEmptyListIsReturned() throws JFException {
            getBarsStub()
                .thenThrow(jfException)
                .thenThrow(jfException);

            subscribeFetch();

            advanceRetryTime(2);

            testSubscriber
                .assertComplete()
                .assertValue(List::isEmpty);

            verifyGetBarsCall(2);
        }

        @Test
        public void whenFetchFailsOneTmeTheBarsFromHistoryAreReturned() throws JFException {
            getBarsStub()
                .thenThrow(jfException)
                .thenReturn(barsFromHistory);

            subscribeFetch();

            advanceRetryTime(2);

            testSubscriber
                .assertComplete()
                .assertValue(barsFromHistory);

            verifyGetBarsCall(2);
        }
    }

    public class FetchTicks {

        private final List<ITick> ticksFromHistory = new ArrayList<>();
        private TestObserver<List<ITick>> testSubscriber;

        @Before
        public void setUp() {
            ticksFromHistory.add(tickMock);
        }

        private OngoingStubbing<List<ITick>> getOrdersStub() throws JFException {
            return when(historyMock.getTicks(fetchInstrument,
                                             startTime,
                                             endTime));
        }

        private void verifyGetTicksCall(final int times) throws JFException {
            verify(historyMock, times(times)).getTicks(fetchInstrument,
                                                       startTime,
                                                       endTime);
        }

        private void subscribeFetch() {
            testSubscriber = historyProvider
                .fetchTicks(fetchInstrument,
                            startTime,
                            endTime)
                .test();
        }

        @Test
        public void whenFetchIsOK() throws JFException {
            getOrdersStub().thenReturn(ticksFromHistory);

            subscribeFetch();

            testSubscriber
                .assertComplete()
                .assertValue(ticksFromHistory);
        }

        @Test
        public void whenFetchFailsTwoTimesAnEmptyListIsReturned() throws JFException {
            getOrdersStub()
                .thenThrow(jfException)
                .thenThrow(jfException);

            subscribeFetch();

            advanceRetryTime(2);

            testSubscriber
                .assertComplete()
                .assertValue(List::isEmpty);

            verifyGetTicksCall(2);
        }

        @Test
        public void whenFetchFailsOneTmeTheTicksFromHistoryAreReturned() throws JFException {
            getOrdersStub()
                .thenThrow(jfException)
                .thenReturn(ticksFromHistory);

            subscribeFetch();

            advanceRetryTime(2);

            testSubscriber
                .assertComplete()
                .assertValue(ticksFromHistory);

            verifyGetTicksCall(2);
        }
    }

    public class FetchBarStart {

        private final long barTime = 9L;
        private final long barStartFromHistory = 12L;
        private TestObserver<Long> testSubscriber;

        private OngoingStubbing<Long> getOrdersStub() throws JFException {
            return when(historyMock.getBarStart(period, barTime));
        }

        private void verifyOrdersByInstrumentCall(final int times) throws JFException {
            verify(historyMock, times(times)).getBarStart(period, barTime);
        }

        private void subscribeFetch() {
            testSubscriber = historyProvider
                .fetchBarStart(period, barTime)
                .test();
        }

        @Test
        public void whenFetchIsOK() throws JFException {
            getOrdersStub().thenReturn(barStartFromHistory);

            subscribeFetch();

            testSubscriber
                .assertComplete()
                .assertValue(barStartFromHistory);
        }

        @Test
        public void whenFetchFailsTwoTimesAZeroTimeIsReturned() throws JFException {
            getOrdersStub()
                .thenThrow(jfException)
                .thenThrow(jfException);

            subscribeFetch();

            advanceRetryTime(2);

            testSubscriber
                .assertComplete()
                .assertValue(0L);

            verifyOrdersByInstrumentCall(2);
        }

        @Test
        public void whenFetchFailsOneTmeTheBarStartFromHistoryIsReturned() throws JFException {
            getOrdersStub()
                .thenThrow(jfException)
                .thenReturn(barStartFromHistory);

            subscribeFetch();

            advanceRetryTime(2);

            testSubscriber
                .assertComplete()
                .assertValue(barStartFromHistory);

            verifyOrdersByInstrumentCall(2);
        }
    }

    public class OrdersByInstrument {

        private final long from = 9L;
        private final long to = 15L;
        private final List<IOrder> ordersFromHistory = new ArrayList<>();
        private TestObserver<List<IOrder>> testSubscriber;

        @Before
        public void setUp() {
            ordersFromHistory.add(orderMock);
        }

        private OngoingStubbing<List<IOrder>> getOrdersStub() throws JFException {
            return when(historyMock.getOrdersHistory(fetchInstrument,
                                                     from,
                                                     to));
        }

        private void verifyOrdersByInstrumentCall(final int times) throws JFException {
            verify(historyMock, times(times)).getOrdersHistory(fetchInstrument,
                                                               from,
                                                               to);
        }

        private void subscribeFetch() {
            testSubscriber = historyProvider
                .ordersByInstrument(fetchInstrument,
                                    from,
                                    to)
                .test();
        }

        @Test
        public void whenFetchIsOK() throws JFException {
            getOrdersStub().thenReturn(ordersFromHistory);

            subscribeFetch();

            testSubscriber
                .assertComplete()
                .assertValue(ordersFromHistory);
        }

        @Test
        public void whenFetchFailsTwoTimesAnEmptyOrderListIsReturned() throws JFException {
            getOrdersStub()
                .thenThrow(jfException)
                .thenThrow(jfException);

            subscribeFetch();

            advanceRetryTime(2);

            testSubscriber
                .assertComplete()
                .assertValue(List::isEmpty);

            verifyOrdersByInstrumentCall(2);
        }

        @Test
        public void whenFetchFailsOneTmeTheOrdersFromHistoryAreReturned() throws JFException {
            getOrdersStub()
                .thenThrow(jfException)
                .thenReturn(ordersFromHistory);

            subscribeFetch();

            advanceRetryTime(2);

            testSubscriber
                .assertComplete()
                .assertValue(ordersFromHistory);

            verifyOrdersByInstrumentCall(2);
        }
    }
}
