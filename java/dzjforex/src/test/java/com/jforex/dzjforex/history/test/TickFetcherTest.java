package com.jforex.dzjforex.history.test;

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

import com.dukascopy.api.ITick;
import com.google.common.collect.Lists;
import com.jforex.dzjforex.brokerhistory.BrokerHistoryData;
import com.jforex.dzjforex.brokerhistory.TickFetcher;
import com.jforex.dzjforex.history.HistoryProvider;
import com.jforex.dzjforex.test.util.CommonUtilForTest;
import com.jforex.programming.quote.TickQuote;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class TickFetcherTest extends CommonUtilForTest {

    private TickFetcher tickFetcher;

    @Mock
    private HistoryProvider historyProviderMock;
    @Mock
    private BrokerHistoryData brokerHistoryDataMock;
    @Captor
    private ArgumentCaptor<List<TickQuote>> tickQuotesCaptor;
    @Mock
    private ITick tickAMock;
    @Mock
    private ITick tickBMock;
    @Mock
    private ITick tickCMock;

    private final long tickATime = 12L;
    private final long tickBTime = 14L;
    private final long tickCTime = 17L;

    private final long endTimeForTick = 42L;
    private final long startTimeForTick = 21L;
    private final int noOfRequestedTicks = 300;
    private final List<ITick> tickMockList = Lists.newArrayList();

    @Before
    public void setUp() {
        setUpMocks();

        tickMockList.add(tickAMock);
        tickMockList.add(tickBMock);
        tickMockList.add(tickCMock);

        tickFetcher = new TickFetcher(historyProviderMock);
    }

    private void setUpMocks() {
        when(brokerHistoryDataMock.endTimeForTick()).thenReturn(endTimeForTick);
        when(brokerHistoryDataMock.noOfRequestedTicks()).thenReturn(noOfRequestedTicks);
        when(brokerHistoryDataMock.startTimeForTick()).thenReturn(startTimeForTick);
        doNothing().when(brokerHistoryDataMock).fillTickQuotes(tickQuotesCaptor.capture());

        when(tickAMock.getTime()).thenReturn(tickATime);
        when(tickBMock.getTime()).thenReturn(tickBTime);
        when(tickCMock.getTime()).thenReturn(tickCTime);
    }

    private TestObserver<Integer> subscribe() {
        return tickFetcher
            .run(instrumentForTest, brokerHistoryDataMock)
            .test();
    }

    private void setHistoryProviderResult(final Single<List<ITick>> result) {
        when(historyProviderMock.ticksByShift(instrumentForTest,
                                              endTimeForTick,
                                              noOfRequestedTicks - 1))
                                                  .thenReturn(result);
    }

    @Test
    public void whenHistoryProviderFailsTheErrorIsPropagated() {
        setHistoryProviderResult(Single.error(jfException));

        subscribe().assertError(jfException);
    }

    public class WhenHistoryProviderSucceeds {

        @Before
        public void setUp() {
            setHistoryProviderResult(Single.just(tickMockList));
        }

        private void setTickStartTime(final long startTimeForTick) {
            when(brokerHistoryDataMock.startTimeForTick()).thenReturn(startTimeForTick);
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
                setTickStartTime(1L);
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
                setTickStartTime(55L);
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
                setTickStartTime(13L);
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
