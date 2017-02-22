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
    private final long endTime = 4200000L;
    private final int nTicks = 5;
    private final Period period = Period.ONE_MIN;
    private BarFetchTimes barFetchTimes;
    private final long expectedStartTime = 3960000L;

    @Before
    public void setUp() {
        when(historyProviderMock.getBarStart(period, expectedStartTime))
            .thenReturn(expectedStartTime);
        when(historyProviderMock.getBarStart(period, endTime))
            .thenReturn(endTime);

        barFetchTimeCalculator = new BarFetchTimeCalculator(historyProviderMock);

        barFetchTimes = barFetchTimeCalculator.calculate(endTime,
                                                         nTicks,
                                                         period);
    }

    @Test
    public void endTimeIsCorrect() {
        assertThat(barFetchTimes.endTime(), equalTo(endTime));

        verify(historyProviderMock).getBarStart(period, endTime);
    }

    @Test
    public void startTimeIsCorrect() {
        assertThat(barFetchTimes.startTime(), equalTo(expectedStartTime));
    }
}
