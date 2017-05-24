package com.jforex.dzjforex.test.util;

import java.util.List;

import org.mockito.Mock;

import com.dukascopy.api.IBar;
import com.dukascopy.api.ITick;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.google.common.collect.Lists;
import com.jforex.programming.quote.BarParams;
import com.jforex.programming.quote.BarQuote;
import com.jforex.programming.quote.TickQuote;

public class BarsAndTicksForTest extends CommonUtilForTest {

    @Mock
    protected IBar barAMock;
    @Mock
    protected IBar barBMock;
    @Mock
    protected IBar barCMock;
    @Mock
    protected ITick tickAMock;
    @Mock
    protected ITick tickBMock;
    @Mock
    protected ITick tickCMock;

    protected BarQuote barQuoteA;
    protected BarQuote barQuoteB;
    protected BarQuote barQuoteC;

    protected TickQuote tickQuoteA;
    protected TickQuote tickQuoteB;
    protected TickQuote tickQuoteC;

    protected final long barATime = 12L;
    protected final long barBTime = 14L;
    protected final long barCTime = 17L;

    protected final long tickATime = 12L;
    protected final long tickBTime = 14L;
    protected final long tickCTime = 17L;

    protected final Period period = Period.ONE_MIN;
    protected final OfferSide offerSide = OfferSide.ASK;
    protected final List<IBar> barMockList = Lists.newArrayList();
    protected final List<ITick> tickMockList = Lists.newArrayList();
    protected final List<BarQuote> barQuoteList = Lists.newArrayList();
    protected final List<TickQuote> tickQuoteList = Lists.newArrayList();

    protected final BarParams barParams = BarParams
        .forInstrument(instrumentForTest)
        .period(period)
        .offerSide(offerSide);
    protected long periodInterval = barParams
        .period()
        .getInterval();

    protected BarsAndTicksForTest() {
        setUpMocks();

        barMockList.add(barAMock);
        barMockList.add(barBMock);
        barMockList.add(barCMock);

        tickMockList.add(tickAMock);
        tickMockList.add(tickBMock);
        tickMockList.add(tickCMock);

        barQuoteA = new BarQuote(barAMock, barParams);
        barQuoteB = new BarQuote(barBMock, barParams);
        barQuoteB = new BarQuote(barCMock, barParams);
        barQuoteList.add(barQuoteA);
        barQuoteList.add(barQuoteB);
        barQuoteList.add(barQuoteC);

        tickQuoteA = new TickQuote(instrumentForTest, tickAMock);
        tickQuoteB = new TickQuote(instrumentForTest, tickBMock);
        tickQuoteC = new TickQuote(instrumentForTest, tickCMock);
        tickQuoteList.add(tickQuoteA);
        tickQuoteList.add(tickQuoteB);
        tickQuoteList.add(tickQuoteC);
    }

    private void setUpMocks() {
        when(barAMock.getTime()).thenReturn(barATime);
        when(barBMock.getTime()).thenReturn(barBTime);
        when(barCMock.getTime()).thenReturn(barCTime);

        when(tickAMock.getTime()).thenReturn(tickATime);
        when(tickBMock.getTime()).thenReturn(tickBTime);
        when(tickCMock.getTime()).thenReturn(tickCTime);
    }
}
