package com.jforex.dzjforex.brokertime.test;
//package com.jforex.dzjforex.time.test;
//
//import static org.hamcrest.Matchers.equalTo;
//import static org.junit.Assert.assertThat;
//
//import java.time.Clock;
//import java.util.HashMap;
//import java.util.Map;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.Mock;
//
//import com.dukascopy.api.ITick;
//import com.dukascopy.api.Instrument;
//import com.jforex.dzjforex.test.util.CommonUtilForTest;
//import com.jforex.dzjforex.time.TickTimeProvider;
//import com.jforex.programming.quote.TickQuote;
//import com.jforex.programming.quote.TickQuoteRepository;
//
//import de.bechte.junit.runners.context.HierarchicalContextRunner;
//
//@RunWith(HierarchicalContextRunner.class)
//public class TickTimeProviderTest extends CommonUtilForTest {
//
//    private TickTimeProvider tickTimeProvider;
//
//    @Mock
//    private TickQuoteRepository tickQuoteRepositoryMock;
//    @Mock
//    private Clock clockMock;
//    @Mock
//    private ITick tickEURUSDMock;
//    @Mock
//    private ITick tickAUDUSDMock;
//    @Mock
//    private ITick tickGBPJPYMock;
//    private final Map<Instrument, TickQuote> quoteByInstrument = new HashMap<>();
//    private TickQuote quoteEURUSD;
//    private TickQuote quoteAUDUSD;
//    private TickQuote quoteGBPJPY;
//    private static final long tickTimeEURUSD = 1L;
//    private static final long tickTimeAUDUSD = 3L;
//    private static final long tickTimeGBPJPY = 12L;
//    private static final long currentTime = 42L;
//
//    @Before
//    public void setUp() {
//        when(tickQuoteRepositoryMock.getAll()).thenReturn(quoteByInstrument);
//        when(clockMock.millis()).thenReturn(currentTime);
//
//        tickTimeProvider = new TickTimeProvider(tickQuoteRepositoryMock, clockMock);
//    }
//
//    @Test
//    public void forNoQuotesTheTickTimeIsZero() {
//        assertThat(tickTimeProvider.get(), equalTo(0L));
//    }
//
//    public class WithQuotesAvailable {
//
//        @Before
//        public void setUp() {
//            when(tickEURUSDMock.getTime()).thenReturn(tickTimeEURUSD);
//            when(tickAUDUSDMock.getTime()).thenReturn(tickTimeAUDUSD);
//
//            quoteEURUSD = new TickQuote(Instrument.EURUSD, tickEURUSDMock);
//            quoteAUDUSD = new TickQuote(Instrument.AUDUSD, tickAUDUSDMock);
//
//            quoteByInstrument.put(Instrument.EURUSD, quoteEURUSD);
//            quoteByInstrument.put(Instrument.AUDUSD, quoteAUDUSD);
//        }
//
//        @Test
//        public void getReturnsLatestQuoteTimeAUDUSD() {
//            assertThat(tickTimeProvider.get(), equalTo(tickTimeAUDUSD));
//        }
//
//        public class WhenGetCalled {
//
//            @Before
//            public void setUp() {
//                tickTimeProvider.get();
//            }
//
//            @Test
//            public void nextGetReturnsLatestQuoteTimeGBPJPYPlusElapsedClockTime() {
//                final long nextCurrentTime = 90L;
//                when(clockMock.millis()).thenReturn(nextCurrentTime);
//                final long passedTime = nextCurrentTime - currentTime;
//
//                assertThat(tickTimeProvider.get(), equalTo(tickTimeAUDUSD + passedTime));
//            }
//
//            @Test
//            public void nextGetReturnsLatestQuoteTimeGBPJPY() {
//                when(tickGBPJPYMock.getTime()).thenReturn(tickTimeGBPJPY);
//                quoteGBPJPY = new TickQuote(Instrument.GBPJPY, tickGBPJPYMock);
//                quoteByInstrument.put(Instrument.GBPJPY, quoteGBPJPY);
//
//                assertThat(tickTimeProvider.get(), equalTo(tickTimeGBPJPY));
//            }
//        }
//    }
//}
