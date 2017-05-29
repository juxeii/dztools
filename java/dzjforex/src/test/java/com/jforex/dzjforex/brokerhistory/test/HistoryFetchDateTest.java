package com.jforex.dzjforex.brokerhistory.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IBar;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.jforex.dzjforex.brokerhistory.HistoryFetchDate;
import com.jforex.dzjforex.history.HistoryWrapper;
import com.jforex.dzjforex.testutil.CommonUtilForTest;
import com.jforex.programming.quote.BarParams;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class HistoryFetchDateTest extends CommonUtilForTest {

    private HistoryFetchDate historyFetchDate;

    @Mock
    private HistoryWrapper historyWrapperMock;
    @Mock
    private IBar barMock;
    private final long tickFetchMillis = 10L;

    @Before
    public void setUp() {
        when(pluginConfigMock.tickFetchMillis()).thenReturn(tickFetchMillis);

        historyFetchDate = new HistoryFetchDate(historyWrapperMock, pluginConfigMock);
    }

    public class ForBarTests {

        private final Period period = Period.ONE_MIN;
        private final OfferSide offerSide = OfferSide.ASK;
        private long barTime;
        private long endDateForBar;
        private final long periodInterval = 60000L;
        private final BarParams barParams = BarParams
            .forInstrument(instrumentForTest)
            .period(period)
            .offerSide(offerSide);

        private void setBarTime(final long barTime) {
            when(barMock.getTime()).thenReturn(barTime);
        }

        private TestObserver<Long> subscribe() {
            return historyFetchDate
                .endDateForBar(barParams, endDateForBar)
                .test();
        }

        private void setGetBarResult(final Single<IBar> result) {
            when(historyWrapperMock.getBar(barParams, 1)).thenReturn(result);
        }

        @Test
        public void whenHistoryWrapperrFailsTheErrorIsPropagated() {
            setGetBarResult(Single.error(jfException));

            subscribe().assertError(jfException);
        }

        public class WhenHistoryWrapperSucceeds {

            @Before
            public void setUp() {
                barTime = 42L;
                setBarTime(barTime);
                setGetBarResult(Single.just(barMock));
            }

            @Test
            public void whenEndDateIsBiggerBarTimePlusPeriodInterval() {
                endDateForBar = barTime + periodInterval + 1L;

                subscribe().assertValue(barTime);
            }

            @Test
            public void whenEndDateEqualsBarTimePlusPeriodInterval() {
                endDateForBar = barTime + periodInterval;

                subscribe().assertValue(endDateForBar - periodInterval);
            }

            @Test
            public void whenEndDateIsLowerBarTimePlusPeriodInterval() {
                endDateForBar = barTime + periodInterval - 1;

                subscribe().assertValue(endDateForBar - periodInterval);
            }
        }
    }

    public class ForTickTests {

        private final long latestTickTime = 42L;
        private long endDateForTick;

        private void setLatestTickTimeResult(final Single<Long> result) {
            when(historyWrapperMock.getTimeOfLastTick(instrumentForTest))
                .thenReturn(result);
        }

        private TestObserver<Long> subscribe() {
            return historyFetchDate
                .startDatesForTick(instrumentForTest, endDateForTick)
                .take(3)
                .test();
        }

        private void assertStartTimes(final long adaptedEndTime) {
            subscribe().assertValues(adaptedEndTime - 1 * tickFetchMillis + 1,
                                     adaptedEndTime - 2 * tickFetchMillis + 1,
                                     adaptedEndTime - 3 * tickFetchMillis + 1);
        }

        @Test
        public void whenHistoryWrapperFailsTheErrorIsPropagated() {
            setLatestTickTimeResult(Single.error(jfException));

            subscribe().assertError(jfException);
        }

        public class WhenHistoryWrapperSucceeds {

            @Before
            public void setUp() {
                setLatestTickTimeResult(Single.just(latestTickTime));
            }

            @Test
            public void whenEndDateIsBiggerThanLatestTickTime() {
                endDateForTick = latestTickTime + 1L;

                assertStartTimes(latestTickTime);
            }

            @Test
            public void whenEndDateEqualsLatestTickTime() {
                endDateForTick = latestTickTime;

                assertStartTimes(endDateForTick);
            }

            @Test
            public void whenEndDateIsLowerLatestTickTime() {
                endDateForTick = latestTickTime - 1L;

                assertStartTimes(endDateForTick);
            }
        }
    }
}
