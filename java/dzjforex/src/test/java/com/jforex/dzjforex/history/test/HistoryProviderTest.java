//package com.jforex.dzjforex.history.test;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.Mock;
//import org.mockito.stubbing.OngoingStubbing;
//
//import com.dukascopy.api.IBar;
//import com.dukascopy.api.IHistory;
//import com.dukascopy.api.IOrder;
//import com.dukascopy.api.ITick;
//import com.dukascopy.api.Instrument;
//import com.dukascopy.api.JFException;
//import com.dukascopy.api.OfferSide;
//import com.dukascopy.api.Period;
//import com.jforex.dzjforex.history.HistoryProvider;
//import com.jforex.dzjforex.test.util.CommonUtilForTest;
//import com.jforex.programming.quote.BarParams;
//
//import de.bechte.junit.runners.context.HierarchicalContextRunner;
//import io.reactivex.observers.TestObserver;
//
//@RunWith(HierarchicalContextRunner.class)
//public class HistoryProviderTest extends CommonUtilForTest {
//
//    private HistoryProvider historyProvider;
//
//    @Mock
//    private IHistory historyMock;
//    @Mock
//    private IBar barMock;
//    @Mock
//    private ITick tickMock;
//    @Mock
//    private IOrder orderMock;
//    private final Instrument fetchInstrument = Instrument.EURUSD;
//    private final Period period = Period.ONE_HOUR;
//    private final OfferSide offerSide = OfferSide.ASK;
//    private final BarParams barParams = BarParams
//        .forInstrument(fetchInstrument)
//        .period(period)
//        .offerSide(offerSide);
//    private final long startTime = 12L;
//    private final long endTime = 42L;
//
//    @Before
//    public void setUp() {
//        historyProvider = new HistoryProvider(historyMock, pluginConfigMock);
//    }
//
//    public class FetchBars {
//
//        private final List<IBar> barsFromHistory = new ArrayList<>();
//        private TestObserver<List<IBar>> barsSubscriber;
//
//        private OngoingStubbing<List<IBar>> getBarsStub() throws JFException {
//            return when(historyMock.getBars(fetchInstrument,
//                                            period,
//                                            offerSide,
//                                            startTime,
//                                            endTime));
//        }
//
//        private void verifyGetBarsCall(final int times) throws JFException {
//            verify(historyMock, times(times)).getBars(fetchInstrument,
//                                                      period,
//                                                      offerSide,
//                                                      startTime,
//                                                      endTime);
//        }
//
//        private void subscribeFetch() {
//            barsSubscriber = historyProvider
//                .fetchBars(barParams,
//                           startTime,
//                           endTime)
//                .test();
//        }
//
//        @Test
//        public void barsFromHistoryAreReturnedWhenHistoryDoesNotThrow() throws JFException {
//            getBarsStub().thenReturn(barsFromHistory);
//
//            subscribeFetch();
//
//            barsSubscriber
//                .assertComplete()
//                .assertValue(barsFromHistory);
//
//            verifyGetBarsCall(1);
//        }
//
//        @Test
//        public void errorIsPropagatedWhenHistoryThrows() throws JFException {
//            getBarsStub().thenThrow(jfException);
//
//            subscribeFetch();
//
//            barsSubscriber.assertError(jfException);
//
//            verifyGetBarsCall(1);
//        }
//    }
//
//    public class BarByShift {
//
//        private static final int shift = 1;
//        private TestObserver<IBar> barSubscriber;
//
//        private OngoingStubbing<IBar> getBarStub() throws JFException {
//            return when(historyMock.getBar(fetchInstrument,
//                                           period,
//                                           offerSide,
//                                           shift));
//        }
//
//        private void verifyGetBarCall() throws JFException {
//            verify(historyMock).getBar(fetchInstrument,
//                                       period,
//                                       offerSide,
//                                       shift);
//        }
//
//        private void subscribeFetch() {
//            barSubscriber = historyProvider
//                .barByShift(barParams, shift)
//                .test();
//        }
//
//        @Test
//        public void barFromHistoryIsReturnedWhenHistoryDoesNotThrow() throws JFException {
//            getBarStub().thenReturn(barMock);
//
//            subscribeFetch();
//
//            barSubscriber
//                .assertComplete()
//                .assertValue(barMock);
//
//            verifyGetBarCall();
//        }
//
//        @Test
//        public void errorIsPropagatedWhenHistoryThrows() throws JFException {
//            getBarStub().thenThrow(jfException);
//
//            subscribeFetch();
//
//            barSubscriber.assertError(jfException);
//
//            verifyGetBarCall();
//        }
//    }
//
//    public class FetchTicks {
//
//        private final List<ITick> ticksFromHistory = new ArrayList<>();
//        private TestObserver<List<ITick>> testSubscriber;
//
//        private OngoingStubbing<List<ITick>> getTicksStub() throws JFException {
//            return when(historyMock.getTicks(fetchInstrument,
//                                             startTime,
//                                             endTime));
//        }
//
//        private void verifyGetTicksCall(final int times) throws JFException {
//            verify(historyMock, times(times)).getTicks(fetchInstrument,
//                                                       startTime,
//                                                       endTime);
//        }
//
//        private void subscribeFetch() {
//            testSubscriber = historyProvider
//                .fetchTicks(fetchInstrument,
//                            startTime,
//                            endTime)
//                .test();
//        }
//
//        @Test
//        public void ticksFromHistoryAreReturnedWhenHistoryDoesNotThrow() throws JFException {
//            getTicksStub().thenReturn(ticksFromHistory);
//
//            subscribeFetch();
//
//            testSubscriber
//                .assertComplete()
//                .assertValue(ticksFromHistory);
//
//            verifyGetTicksCall(1);
//        }
//
//        @Test
//        public void errorIsPropagatedWhenHistoryThrows() throws JFException {
//            getTicksStub().thenThrow(jfException);
//
//            subscribeFetch();
//
//            testSubscriber.assertError(jfException);
//
//            verifyGetTicksCall(1);
//        }
//    }
//
//    public class OrdersByInstrument {
//
//        private final long from = 9L;
//        private final long to = 15L;
//        private final List<IOrder> ordersFromHistory = new ArrayList<>();
//        private TestObserver<List<IOrder>> testSubscriber;
//
//        private OngoingStubbing<List<IOrder>> getOrdersHistoryStub() throws JFException {
//            return when(historyMock.getOrdersHistory(fetchInstrument,
//                                                     from,
//                                                     to));
//        }
//
//        private void verifyGetOrdersHistoryCall(final int times) throws JFException {
//            verify(historyMock, times(times)).getOrdersHistory(fetchInstrument,
//                                                               from,
//                                                               to);
//        }
//
//        private void subscribeFetch() {
//            testSubscriber = historyProvider
//                .ordersByInstrument(fetchInstrument,
//                                    from,
//                                    to)
//                .test();
//        }
//
//        @Test
//        public void ordersFromHistoryAreReturnedWhenHistoryDoesNotThrow() throws JFException {
//            getOrdersHistoryStub().thenReturn(ordersFromHistory);
//
//            subscribeFetch();
//
//            testSubscriber
//                .assertComplete()
//                .assertValue(ordersFromHistory);
//
//            verifyGetOrdersHistoryCall(1);
//        }
//
//        @Test
//        public void errorIsPropagatedWhenHistoryThrows() throws JFException {
//            getOrdersHistoryStub().thenThrow(jfException);
//
//            subscribeFetch();
//
//            testSubscriber.assertError(jfException);
//
//            verifyGetOrdersHistoryCall(1);
//        }
//    }
//}
