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

public class BarFetchTimeCalculatorTest extends CommonUtilForTest {

    private BarFetchTimeCalculator barFetchTimeCalculator;

    @Mock
    private HistoryProvider historyProviderMock;
    private final long endTime = 42L;
    private final long previousBarStart = 41L;
    private final int nTicks = 270;
    private final Period period = Period.ONE_MIN;
    private BarFetchTimes barFetchTimes;

    @Before
    public void setUp() {
        when(historyProviderMock.getPreviousBarStart(period, endTime))
            .thenReturn(previousBarStart);

        barFetchTimeCalculator = new BarFetchTimeCalculator(historyProviderMock);

        barFetchTimes = barFetchTimeCalculator.calculate(endTime,
                                                         nTicks,
                                                         period);
    }

    @Test
    public void endTimeIsCorrect() {
        assertThat(barFetchTimes.endTime(), equalTo(previousBarStart));

        verify(historyProviderMock).getPreviousBarStart(period, endTime);
    }

    @Test
    public void startTimeIsCorrect() {
        final int numberOfBarsBeforeEndTimeBar = nTicks - 1;
        final long intervalToStartBarTime = numberOfBarsBeforeEndTimeBar * period.getInterval();
        final long expectedStartTime = previousBarStart - intervalToStartBarTime;

        assertThat(barFetchTimes.startTime(), equalTo(expectedStartTime));
    }
}
