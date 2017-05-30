package com.jforex.dzjforex.brokerhistory.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.jforex.dzjforex.brokerhistory.BrokerHistoryData;
import com.jforex.dzjforex.brokerhistory.HistoryTickFiller;
import com.jforex.dzjforex.brokertime.TimeConvert;
import com.jforex.dzjforex.testutil.BarsAndTicksForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;

@RunWith(HierarchicalContextRunner.class)
public class BrokerHistoryDataTest extends BarsAndTicksForTest {

    private BrokerHistoryData brokerHistoryData;

    @Mock
    private HistoryTickFiller historyTickFillerMock;
    private final double startTime = 123.45;
    private final double endTime = 321.89;
    private final int noOfTickMinutes = 0;
    private final int noOfRequestedTicks = 3;
    private final int zorroTickSize = 7;

    @Before
    public void setUp() {
        brokerHistoryData = new BrokerHistoryData(instrumentNameForTest,
                                                  startTime,
                                                  endTime,
                                                  noOfTickMinutes,
                                                  noOfRequestedTicks,
                                                  historyTickFillerMock);
    }

    @Test
    public void assertInstrumentName() {
        assertThat(brokerHistoryData.assetName(), equalTo(instrumentNameForTest));
    }

    @Test
    public void assertStartTimeForBar() {
        assertThat(brokerHistoryData.startTimeForBar(),
                   equalTo(TimeConvert.millisFromOLEDateRoundMinutes(startTime)));
    }

    @Test
    public void assertEndTimeForBar() {
        assertThat(brokerHistoryData.endTimeForBar(),
                   equalTo(TimeConvert.millisFromOLEDateRoundMinutes(endTime)));
    }

    @Test
    public void assertStartTimeForTick() {
        assertThat(brokerHistoryData.startTimeForTick(),
                   equalTo(TimeConvert.millisFromOLEDate(startTime)));
    }

    @Test
    public void assertEndTimeForTick() {
        assertThat(brokerHistoryData.endTimeForTick(),
                   equalTo(TimeConvert.millisFromOLEDate(endTime) - 2));
    }

    @Test
    public void assertNoOfTickMinutes() {
        assertThat(brokerHistoryData.periodInMinutes(), equalTo(noOfTickMinutes));
    }

    @Test
    public void assertNoOfRequestedTicks() {
        assertThat(brokerHistoryData.noOfRequestedTicks(), equalTo(noOfRequestedTicks));
    }

    @Test
    public void fillBarQuotesCallsCorrectOnHistoryFiller() {
        brokerHistoryData.fillBarQuotes(barQuoteList);

        verify(historyTickFillerMock, times(barQuoteList.size())).fillBarQuote(any(), anyInt());
        verify(historyTickFillerMock).fillBarQuote(barQuoteA, 0 * zorroTickSize);
        verify(historyTickFillerMock).fillBarQuote(barQuoteB, 1 * zorroTickSize);
        verify(historyTickFillerMock).fillBarQuote(barQuoteC, 2 * zorroTickSize);
    }

    @Test
    public void fillTickQuotesCallsCorrectOnHistoryFiller() {
        brokerHistoryData.fillTickQuotes(tickQuoteList);

        verify(historyTickFillerMock, times(tickQuoteList.size())).fillTickQuote(any(), anyInt());
        verify(historyTickFillerMock).fillTickQuote(tickQuoteA, 0 * zorroTickSize);
        verify(historyTickFillerMock).fillTickQuote(tickQuoteB, 1 * zorroTickSize);
        verify(historyTickFillerMock).fillTickQuote(tickQuoteC, 2 * zorroTickSize);
    }
}
