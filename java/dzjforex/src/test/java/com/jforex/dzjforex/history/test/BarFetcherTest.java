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

import com.dukascopy.api.IBar;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.google.common.collect.Lists;
import com.jforex.dzjforex.brokerhistory.BarFetcher;
import com.jforex.dzjforex.brokerhistory.BrokerHistoryData;
import com.jforex.dzjforex.history.HistoryProvider;
import com.jforex.dzjforex.test.util.CommonUtilForTest;
import com.jforex.programming.quote.BarParams;
import com.jforex.programming.quote.BarQuote;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class BarFetcherTest extends CommonUtilForTest {

    private BarFetcher barFetcher;

    @Mock
    private HistoryProvider historyProviderMock;
    @Mock
    private BrokerHistoryData brokerHistoryDataMock;
    @Captor
    private ArgumentCaptor<BarParams> barParamsCaptor;
    @Captor
    private ArgumentCaptor<List<BarQuote>> barQuotesCaptor;
    @Mock
    private IBar barAMock;
    @Mock
    private IBar barBMock;
    @Mock
    private IBar barCMock;

    private final long barATime = 12L;
    private final long barBTime = 14L;
    private final long barCTime = 17L;

    private final Period period = Period.ONE_MIN;
    private final OfferSide offerSide = OfferSide.ASK;
    private final long endTimeForBar = 42L;
    private final long startTimeForBar = 21L;
    private final int noOfRequestedTicks = 300;
    private final int noOfTickMinutes = 1;
    private final List<IBar> barMockList = Lists.newArrayList();

    @Before
    public void setUp() {
        setUpMocks();

        barMockList.add(barAMock);
        barMockList.add(barBMock);
        barMockList.add(barCMock);

        barFetcher = new BarFetcher(historyProviderMock);
    }

    private void setUpMocks() {
        when(brokerHistoryDataMock.endTimeForBar()).thenReturn(endTimeForBar);
        when(brokerHistoryDataMock.noOfRequestedTicks()).thenReturn(noOfRequestedTicks);
        when(brokerHistoryDataMock.startTimeForBar()).thenReturn(startTimeForBar);
        when(brokerHistoryDataMock.noOfTickMinutes()).thenReturn(noOfTickMinutes);
        doNothing().when(brokerHistoryDataMock).fillBarQuotes(barQuotesCaptor.capture());

        when(barAMock.getTime()).thenReturn(barATime);
        when(barBMock.getTime()).thenReturn(barBTime);
        when(barCMock.getTime()).thenReturn(barCTime);
    }

    private TestObserver<Integer> subscribe() {
        return barFetcher
            .run(instrumentForTest, brokerHistoryDataMock)
            .test();
    }

    private void setHistoryProviderResult(final Single<List<IBar>> result) {
        when(historyProviderMock.barsByShift(barParamsCaptor.capture(),
                                             eq(endTimeForBar),
                                             eq(noOfRequestedTicks - 1)))
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
            setHistoryProviderResult(Single.just(barMockList));
        }

        private void setBarStartTime(final long startTimeForBar) {
            when(brokerHistoryDataMock.startTimeForBar()).thenReturn(startTimeForBar);
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
                setBarStartTime(1L);
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
                setBarStartTime(55L);
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
                setBarStartTime(13L);
            }

            @Test
            public void barAIsFiltered() {
                subscribe().assertValue(2);
            }

            @Test
            public void barBAndbarCAreFilledAsQuotes() {
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
