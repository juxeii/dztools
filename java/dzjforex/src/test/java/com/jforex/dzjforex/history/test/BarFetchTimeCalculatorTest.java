package com.jforex.dzjforex.history.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.dukascopy.api.Period;
import com.jforex.dzjforex.history.BarFetchTimeCalculator;
import com.jforex.dzjforex.history.BarFetchTimes;
import com.jforex.dzjforex.history.HistoryProvider;
import com.jforex.dzjforex.test.util.CommonUtilForTest;
import com.jforex.dzjforex.time.TimeConvert;

import io.reactivex.Observable;

public class BarFetchTimeCalculatorTest extends CommonUtilForTest {

    private BarFetchTimeCalculator barFetchTimeCalculator;

    @Mock
    private HistoryProvider historyProviderMock;
    private final double endTimeUTC = 420.34836465;
    private final long endTimeMillis = TimeConvert.millisFromOLEDate(endTimeUTC);
    private final int nTicks = 5;
    private final Period period = Period.ONE_MIN;
    private BarFetchTimes barFetchTimes;
    private final long expectedStartTime = -2172843741294L;

    @Before
    public void setUp() {
        when(historyProviderMock.fetchBarStart(period, expectedStartTime))
            .thenReturn(Observable.just(expectedStartTime));
        when(historyProviderMock.fetchBarStart(period, endTimeMillis))
            .thenReturn(Observable.just(endTimeMillis));

        barFetchTimeCalculator = new BarFetchTimeCalculator(historyProviderMock);

        barFetchTimes = barFetchTimeCalculator.calculate(endTimeUTC,
                                                         nTicks,
                                                         period);
    }

    @Test
    public void endTimeIsCorrect() {
        assertThat(barFetchTimes.endTime(), equalTo(endTimeMillis));

        verify(historyProviderMock).fetchBarStart(period, endTimeMillis);
    }

    @Test
    public void startTimeIsCorrect() {
        assertThat(barFetchTimes.startTime(), equalTo(expectedStartTime));
    }
}
