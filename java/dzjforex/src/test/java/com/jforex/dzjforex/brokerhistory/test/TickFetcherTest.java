package com.jforex.dzjforex.brokerhistory.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.stubbing.OngoingStubbing;

import com.dukascopy.api.ITick;
import com.jforex.dzjforex.brokerhistory.BrokerHistoryData;
import com.jforex.dzjforex.brokerhistory.TickFetcher;
import com.jforex.dzjforex.brokerhistory.TickHistoryByShift;
import com.jforex.dzjforex.testutil.BarsAndTicksForTest;
import com.jforex.programming.quote.TickQuote;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class TickFetcherTest extends BarsAndTicksForTest {

    private TickFetcher tickFetcher;

    @Mock
    private TickHistoryByShift tickHistoryByShiftMock;
    @Mock
    private BrokerHistoryData brokerHistoryDataMock;
    @Captor
    private ArgumentCaptor<List<TickQuote>> tickQuotesCaptor;

    private final long endTimeForTick = 42L;
    private final long startTimeForTick = 21L;
    private final int noOfRequestedTicks = 300;

    @Before
    public void setUp() {
        setUpMocks();

        tickFetcher = new TickFetcher(tickHistoryByShiftMock);
    }

    private void setUpMocks() {
        when(brokerHistoryDataMock.endTimeForTick()).thenReturn(endTimeForTick);
        when(brokerHistoryDataMock.noOfRequestedTicks()).thenReturn(noOfRequestedTicks);
        when(brokerHistoryDataMock.startTimeForTick()).thenReturn(startTimeForTick);
        doNothing().when(brokerHistoryDataMock).fillTickQuotes(tickQuotesCaptor.capture());
    }

    private TestObserver<Integer> subscribe() {
        return tickFetcher
            .run(instrumentForTest, brokerHistoryDataMock)
            .test();
    }

    private OngoingStubbing<Single<List<ITick>>> stubHistoryShift() {
        return when(tickHistoryByShiftMock.get(instrumentForTest,
                                               endTimeForTick,
                                               noOfRequestedTicks - 1));
    }

    @Test
    public void runCallIsDeferred() {
        tickFetcher.run(instrumentForTest, brokerHistoryDataMock);

        verifyZeroInteractions(tickHistoryByShiftMock);
    }

    @Test
    public void whenTickHistoryByShiftFailsTheErrorIsPropagated() {
        stubHistoryShift().thenReturn(Single.error(jfException));

        subscribe().assertError(jfException);
    }

    public class WhenTickHistoryByShiftSucceeds {

        @Before
        public void setUp() {
            stubHistoryShift().thenReturn(Single.just(tickMockList));
        }

        private OngoingStubbing<Long> stubTickStartTime() {
            return when(brokerHistoryDataMock.startTimeForTick());
        }

        private void assertTickQuote(final TickQuote tickQuote,
                                     final ITick tick) {
            final ITick tickFromQuote = tickQuote.tick();

            assertThat(tickQuote.instrument(), equalTo(instrumentForTest));
            assertThat(tickFromQuote.getTime(), equalTo(tick.getTime()));
        }

        public class WhenStartTimeIsSmallerThanAllTicks {

            @Before
            public void setUp() {
                stubTickStartTime().thenReturn(1L);
            }

            @Test
            public void noTickIsFiltered() {
                subscribe().assertValue(tickMockList.size());
            }

            @Test
            public void quotesAreCorrectFilled() {
                subscribe();

                final List<TickQuote> tickQuotes = tickQuotesCaptor.getValue();
                assertThat(tickQuotes.size(), equalTo(3));
                assertTickQuote(tickQuotes.get(0), tickAMock);
                assertTickQuote(tickQuotes.get(1), tickBMock);
                assertTickQuote(tickQuotes.get(2), tickCMock);
            }

            @Test
            public void verifyFillCall() {
                subscribe();

                verify(brokerHistoryDataMock).fillTickQuotes(tickQuotesCaptor.getValue());
            }
        }

        public class WhenStartTimeIsBiggerThanAllTicks {

            @Before
            public void setUp() {
                stubTickStartTime().thenReturn(55L);
            }

            @Test
            public void allTicksAreFiltered() {
                subscribe().assertValue(0);
            }

            @Test
            public void noQuotesAreFilled() {
                subscribe();

                final List<TickQuote> tickQuotes = tickQuotesCaptor.getValue();
                assertTrue(tickQuotes.isEmpty());
            }

            @Test
            public void verifyFillCall() {
                subscribe();

                verify(brokerHistoryDataMock).fillTickQuotes(tickQuotesCaptor.getValue());
            }
        }

        public class WhenStartTimeIsBiggerThanTickA {

            @Before
            public void setUp() {
                stubTickStartTime().thenReturn(13L);
            }

            @Test
            public void tickAIsFiltered() {
                subscribe().assertValue(2);
            }

            @Test
            public void tickBAndtickCAreFilledAsQuotes() {
                subscribe();

                final List<TickQuote> tickQuotes = tickQuotesCaptor.getValue();
                assertThat(tickQuotes.size(), equalTo(2));
                assertTickQuote(tickQuotes.get(0), tickBMock);
                assertTickQuote(tickQuotes.get(1), tickCMock);
            }

            @Test
            public void verifyFillCall() {
                subscribe();

                verify(brokerHistoryDataMock).fillTickQuotes(tickQuotesCaptor.getValue());
            }
        }
    }
}
