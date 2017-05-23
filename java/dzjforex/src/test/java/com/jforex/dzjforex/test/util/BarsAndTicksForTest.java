package com.jforex.dzjforex.test.util;

import java.util.List;

import org.mockito.Mock;

import com.dukascopy.api.IBar;
import com.dukascopy.api.ITick;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.google.common.collect.Lists;
import com.jforex.programming.quote.BarParams;

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
