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

import com.dukascopy.api.IBar;
import com.jforex.dzjforex.brokerhistory.BarFetcher;
import com.jforex.dzjforex.brokerhistory.BarHistoryByShift;
import com.jforex.dzjforex.brokerhistory.BrokerHistoryData;
import com.jforex.dzjforex.testutil.BarsAndTicksForTest;
import com.jforex.programming.quote.BarParams;
import com.jforex.programming.quote.BarQuote;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class BarFetcherTest extends BarsAndTicksForTest {

    private BarFetcher barFetcher;

    @Mock
    private BarHistoryByShift barHistoryByShiftMock;
    @Mock
    private BrokerHistoryData brokerHistoryDataMock;
    @Captor
    private ArgumentCaptor<BarParams> barParamsCaptor;
    @Captor
    private ArgumentCaptor<List<BarQuote>> barQuotesCaptor;

    private final long endTimeForBar = 42L;
    private final long startTimeForBar = 21L;
    private final int noOfRequestedTicks = 300;
    private final int noOfTickMinutes = 1;

    @Before
    public void setUp() {
        setUpMocks();

        barFetcher = new BarFetcher(barHistoryByShiftMock);
    }

    private void setUpMocks() {
        when(brokerHistoryDataMock.endTimeForBar()).thenReturn(endTimeForBar);
        when(brokerHistoryDataMock.noOfRequestedTicks()).thenReturn(noOfRequestedTicks);
        when(brokerHistoryDataMock.startTimeForBar()).thenReturn(startTimeForBar);
        when(brokerHistoryDataMock.noOfTickMinutes()).thenReturn(noOfTickMinutes);
        doNothing().when(brokerHistoryDataMock).fillBarQuotes(barQuotesCaptor.capture());
    }

    private TestObserver<Integer> subscribe() {
        return barFetcher
            .run(instrumentForTest, brokerHistoryDataMock)
            .test();
    }

    private OngoingStubbing<Single<List<IBar>>> stubHistoryShift() {
        return when(barHistoryByShiftMock.get(barParamsCaptor.capture(),
                                              eq(endTimeForBar),
                                              eq(noOfRequestedTicks - 1)));
    }

    @Test
    public void runCallIsDeferred() {
        barFetcher.run(instrumentForTest, brokerHistoryDataMock);

        verifyZeroInteractions(barHistoryByShiftMock);
    }

    @Test
    public void whenHistoryProviderFailsTheErrorIsPropagated() {
        stubHistoryShift().thenReturn(Single.error(jfException));

        subscribe().assertError(jfException);
    }

    public class WhenHistoryProviderSucceeds {

        @Before
        public void setUp() {
            stubHistoryShift().thenReturn(Single.just(barMockList));
        }

        private OngoingStubbing<Long> stubBarStartTime() {
            return when(brokerHistoryDataMock.startTimeForBar());
        }

        private void assertBarParams() {
            final BarParams barParams = barParamsCaptor.getValue();

            assertThat(barParams.instrument(), equalTo(instrumentForTest));
            assertThat(barParams.period(), equalTo(period));
            assertThat(barParams.offerSide(), equalTo(offerSide));
        }

        private void assertBarQuote(final BarQuote barQuote,
                                    final IBar bar) {
            final IBar barFromQuote = barQuote.bar();

            assertThat(barQuote.instrument(), equalTo(instrumentForTest));
            assertThat(barFromQuote.getTime(), equalTo(bar.getTime()));
            assertThat(barQuote.barParams(), equalTo(barParamsCaptor.getValue()));
        }

        @Test
        public void barParamsAreCorrectForHistoryProviderCall() {
            subscribe();

            assertBarParams();
        }

        public class WhenStartTimeIsSmallerThanAllBars {

            @Before
            public void setUp() {
                stubBarStartTime().thenReturn(1L);
            }

            @Test
            public void noBarIsFiltered() {
                subscribe().assertValue(barMockList.size());
            }

            @Test
            public void quotesAreCorrectFilled() {
                subscribe();

                final List<BarQuote> barQuotes = barQuotesCaptor.getValue();
                assertThat(barQuotes.size(), equalTo(3));
                assertBarQuote(barQuotes.get(0), barAMock);
                assertBarQuote(barQuotes.get(1), barBMock);
                assertBarQuote(barQuotes.get(2), barCMock);
            }

            @Test
            public void verifyFillCall() {
                subscribe();

                verify(brokerHistoryDataMock).fillBarQuotes(barQuotesCaptor.getValue());
            }
        }

        public class WhenStartTimeIsBiggerThanAllBars {

            @Before
            public void setUp() {
                stubBarStartTime().thenReturn(55L);
            }

            @Test
            public void allBarsAreFiltered() {
                subscribe().assertValue(0);
            }

            @Test
            public void noQuotesAreFilled() {
                subscribe();

                final List<BarQuote> barQuotes = barQuotesCaptor.getValue();
                assertTrue(barQuotes.isEmpty());
            }

            @Test
            public void verifyFillCall() {
                subscribe();

                verify(brokerHistoryDataMock).fillBarQuotes(barQuotesCaptor.getValue());
            }
        }

        public class WhenStartTimeIsBiggerThanBarA {

            @Before
            public void setUp() {
                stubBarStartTime().thenReturn(13L);
            }

            @Test
            public void barAIsFiltered() {
                subscribe().assertValue(2);
            }

            @Test
            public void barBAndBarCAreFilledAsQuotes() {
                subscribe();

                final List<BarQuote> barQuotes = barQuotesCaptor.getValue();
                assertThat(barQuotes.size(), equalTo(2));
                assertBarQuote(barQuotes.get(0), barBMock);
                assertBarQuote(barQuotes.get(1), barCMock);
            }

            @Test
            public void verifyFillCall() {
                subscribe();

                verify(brokerHistoryDataMock).fillBarQuotes(barQuotesCaptor.getValue());
            }
        }
    }
}
