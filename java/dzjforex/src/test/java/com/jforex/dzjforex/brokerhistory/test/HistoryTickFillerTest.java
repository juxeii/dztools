package com.jforex.dzjforex.brokerhistory.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.jforex.dzjforex.brokerhistory.HistoryTickFiller;
import com.jforex.dzjforex.brokertime.TimeConvert;
import com.jforex.dzjforex.test.util.BarsAndTicksForTest;
import com.jforex.programming.math.MathUtil;

import de.bechte.junit.runners.context.HierarchicalContextRunner;

@RunWith(HierarchicalContextRunner.class)
public class HistoryTickFillerTest extends BarsAndTicksForTest {

    private HistoryTickFiller historyTickFiller;

    private final int zorroTickSize = 7;
    private final double tickParams[] = new double[2 * zorroTickSize];
    private final double barAHigh = 1.12345;
    private final double barALow = 1.123;
    private final double barAOpen = 1.12234;
    private final double barAClose = 1.1244;
    private final double barAVolume = 233.45;

    private final double tickAAsk = 1.12345;
    private final double tickABid = 1.11345;
    private final double tickAAskVolume = 234;

    @Before
    public void setUp() {
        setUpMocks();

        historyTickFiller = new HistoryTickFiller(tickParams);

        historyTickFiller.fillBarQuote(barQuoteA, 0);
        historyTickFiller.fillTickQuote(tickQuoteA, 0);
    }

    private void setUpMocks() {
        when(barAMock.getOpen()).thenReturn(barAOpen);
        when(barAMock.getClose()).thenReturn(barAClose);
        when(barAMock.getHigh()).thenReturn(barAHigh);
        when(barAMock.getLow()).thenReturn(barALow);
        when(barAMock.getVolume()).thenReturn(barAVolume);

        when(tickAMock.getTime()).thenReturn(tickATime);
        when(tickAMock.getAsk()).thenReturn(tickAAsk);
        when(tickAMock.getBid()).thenReturn(tickABid);
        when(tickAMock.getAskVolume()).thenReturn(tickAAskVolume);
    }

    private void fillBarQuoteForStartIndex(final int startIndex) {
        historyTickFiller.fillBarQuote(barQuoteA, startIndex);
    }

    private void fillTickQuoteForStartIndex(final int startIndex) {
        historyTickFiller.fillTickQuote(tickQuoteA, startIndex);
    }

    private void assertBarFill(final int startIndex) {
        assertThat(tickParams[startIndex], equalTo(barAOpen));
        assertThat(tickParams[startIndex + 1], equalTo(barAClose));
        assertThat(tickParams[startIndex + 2], equalTo(barAHigh));
        assertThat(tickParams[startIndex + 3], equalTo(barALow));
        assertThat(tickParams[startIndex + 4], equalTo(TimeConvert.getUTCTimeFromBar(barAMock)));
        assertThat(tickParams[startIndex + 5], equalTo(0.0));
        assertThat(tickParams[startIndex + 6], equalTo(barAVolume));
    }

    private void assertTickFill(final int startIndex) {
        assertThat(tickParams[startIndex], equalTo(tickAAsk));
        assertThat(tickParams[startIndex + 1], equalTo(tickAAsk));
        assertThat(tickParams[startIndex + 2], equalTo(tickAAsk));
        assertThat(tickParams[startIndex + 3], equalTo(tickAAsk));
        assertThat(tickParams[startIndex + 4], equalTo(TimeConvert.getUTCTimeFromTick(tickAMock)));
        assertThat(tickParams[startIndex + 5], equalTo(MathUtil.roundPrice(tickAAsk - tickABid, instrumentForTest)));
        assertThat(tickParams[startIndex + 6], equalTo(tickAAskVolume));
    }

    @Test
    public void barFillICorrectForStartIndexZero() {
        fillBarQuoteForStartIndex(0);

        assertBarFill(0);
    }

    @Test
    public void barFillICorrectForStartIndexOne() {
        fillBarQuoteForStartIndex(1);

        assertBarFill(1);
    }

    @Test
    public void tickFillICorrectForStartIndexZero() {
        fillTickQuoteForStartIndex(0);

        assertTickFill(0);
    }

    @Test
    public void tickFillICorrectForStartIndexOne() {
        fillTickQuoteForStartIndex(1);

        assertTickFill(1);
    }
}
