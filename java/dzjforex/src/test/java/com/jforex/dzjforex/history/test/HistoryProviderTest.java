package com.jforex.dzjforex.history.test;

import org.junit.runner.RunWith;

import com.jforex.dzjforex.test.util.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;

@RunWith(HierarchicalContextRunner.class)
public class HistoryProviderTest extends CommonUtilForTest {

//    private HistoryProvider historyProvider;
//
//    @Mock
//    private IHistory historyMock;
//    @Mock
//    private PluginConfig pluginConfigMock;
//    private final Instrument fetchInstrument = Instrument.EURUSD;
//    private final long startTime = 12L;
//    private final long endTime = 42L;
//    private final int maxRetries = 2;
//    private final JFException historyException = new JFException("");
//
//    @Before
//    public void setUp() {
//        when(pluginConfigMock.historyDownloadRetries()).thenReturn(maxRetries);
//
//        historyProvider = new HistoryProvider(historyMock, pluginConfigMock);
//    }
//
//    public class FetchBars {
//
//        private final Period period = Period.ONE_HOUR;
//        private final OfferSide offerSide = OfferSide.ASK;
//        private final Filter filter = Filter.ALL_FLATS;
//        private OngoingStubbing<List<IBar>> getBarsStub;
//        private List<IBar> fetchedBars;
//        private final List<IBar> barsFromHistory = new ArrayList<>();
//
//        private void callFetch() {
//            fetchedBars = historyProvider.fetchBars(fetchInstrument,
//                                                    period,
//                                                    offerSide,
//                                                    filter,
//                                                    startTime,
//                                                    endTime);
//        }
//
//        public void verifyRetries() throws JFException {
//            verify(historyMock, times(maxRetries + 1)).getBars(fetchInstrument,
//                                                               period,
//                                                               offerSide,
//                                                               filter,
//                                                               startTime,
//                                                               endTime);
//        }
//
//        @Before
//        public void setUp() throws JFException {
//            getBarsStub = when(historyMock.getBars(fetchInstrument,
//                                                   period,
//                                                   offerSide,
//                                                   filter,
//                                                   startTime,
//                                                   endTime));
//        }
//
//        @Test
//        public void returnedListIsFromHistory() {
//            getBarsStub.thenReturn(barsFromHistory);
//
//            callFetch();
//
//            assertThat(fetchedBars, equalTo(barsFromHistory));
//        }
//
//        public class TwoHistoryFailsThenOK {
//
//            @Before
//            public void setUp() {
//                getBarsStub = getBarsStub
//                    .thenThrow(historyException)
//                    .thenThrow(historyException)
//                    .thenReturn(barsFromHistory);
//
//                callFetch();
//            }
//
//            @Test
//            public void fetchedBarsAreFromHistory() {
//                assertThat(fetchedBars, equalTo(barsFromHistory));
//            }
//
//            @Test
//            public void retriesAreCalled() throws JFException {
//                verifyRetries();
//            }
//        }
//
//        public class HistoryFailsComplete {
//
//            @Before
//            public void setUp() {
//                getBarsStub = getBarsStub
//                    .thenThrow(historyException)
//                    .thenThrow(historyException)
//                    .thenThrow(historyException);
//
//                callFetch();
//            }
//
//            @Test
//            public void fetchedBarsAreNew() {
//                assertFalse(fetchedBars == barsFromHistory);
//                assertTrue(fetchedBars.isEmpty());
//            }
//
//            @Test
//            public void retriesAreCalled() throws JFException {
//                verifyRetries();
//            }
//        }
//    }
//
//    public class FetchTick {
//
//        private OngoingStubbing<List<ITick>> getTicksStub;
//        private List<ITick> fetchedTicks;
//        private final List<ITick> ticksFromHistory = new ArrayList<>();
//
//        private void callFetch() {
//            fetchedTicks = historyProvider.fetchTicks(fetchInstrument,
//                                                      startTime,
//                                                      endTime);
//        }
//
//        public void verifyRetries() throws JFException {
//            verify(historyMock, times(maxRetries + 1)).getTicks(fetchInstrument,
//                                                                startTime,
//                                                                endTime);
//        }
//
//        @Before
//        public void setUp() throws JFException {
//            getTicksStub = when(historyMock.getTicks(fetchInstrument,
//                                                     startTime,
//                                                     endTime));
//        }
//
//        @Test
//        public void returnedListIsFromHistory() {
//            getTicksStub.thenReturn(ticksFromHistory);
//
//            callFetch();
//
//            assertThat(fetchedTicks, equalTo(ticksFromHistory));
//        }
//
//        public class TwoHistoryFailsThenOK {
//
//            @Before
//            public void setUp() {
//                getTicksStub = getTicksStub
//                    .thenThrow(historyException)
//                    .thenThrow(historyException)
//                    .thenReturn(ticksFromHistory);
//
//                callFetch();
//            }
//
//            @Test
//            public void fetchedTicksAreFromHistory() {
//                assertThat(fetchedTicks, equalTo(ticksFromHistory));
//            }
//
//            @Test
//            public void retriesAreCalled() throws JFException {
//                verifyRetries();
//            }
//        }
//
//        public class HistoryFailsComplete {
//
//            @Before
//            public void setUp() {
//                getTicksStub = getTicksStub
//                    .thenThrow(historyException)
//                    .thenThrow(historyException)
//                    .thenThrow(historyException);
//
//                callFetch();
//            }
//
//            @Test
//            public void fetchedTicksAreNew() {
//                assertFalse(fetchedTicks == ticksFromHistory);
//                assertTrue(fetchedTicks.isEmpty());
//            }
//
//            @Test
//            public void retriesAreCalled() throws JFException {
//                verifyRetries();
//            }
//        }
//    }
}
