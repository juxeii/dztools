package com.jforex.dzjforex.history.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.history.HistoryProvider;
import com.jforex.dzjforex.history.TickFetcher;
import com.jforex.dzjforex.test.util.CommonUtilForTest;
import com.jforex.dzjforex.time.DateTimeUtils;
import com.jforex.programming.math.MathUtil;

import de.bechte.junit.runners.context.HierarchicalContextRunner;

@RunWith(HierarchicalContextRunner.class)
public class TickFetcherTest extends CommonUtilForTest {

    private TickFetcher tickFetcher;

    @Mock
    private HistoryProvider historyProviderMock;
    @Mock
    private ITick tickAMock;
    @Mock
    private ITick tickBMock;
    private final double tickAAsk = 1.32123;
    private final double tickBAsk = 1.32126;

    private final double tickABid = 1.32107;
    private final double tickBBid = 1.32111;

    private final long tickATime = 12L;
    private final long tickBTime = 14L;

    private final double tickAVol = 223.34;
    private final double tickBVol = 250.45;

    private final Instrument fetchInstrument = Instrument.EURUSD;
    private final double startDate = 12.4;
    private final double endDate = 13.7;
    private final int tickMinutes = 0;
    private final int nTicks = 270;
    private double tickParams[];
    private final List<ITick> tickMockList = new ArrayList<>();

    @Before
    public void setUp() {
        setTickExpectations(tickAMock,
                            tickAAsk,
                            tickABid,
                            tickATime,
                            tickAVol);
        setTickExpectations(tickBMock,
                            tickBAsk,
                            tickBBid,
                            tickBTime,
                            tickBVol);

        tickMockList.add(tickAMock);
        tickMockList.add(tickBMock);

        tickParams = new double[tickMockList.size() * 7];

        tickFetcher = new TickFetcher(historyProviderMock);
    }

    private void setTickExpectations(final ITick tickMock,
                                     final double ask,
                                     final double bid,
                                     final long time,
                                     final double volume) {
        when(tickMock.getAsk()).thenReturn(ask);
        when(tickMock.getBid()).thenReturn(bid);
        when(tickMock.getTime()).thenReturn(time);
        when(tickMock.getAskVolume()).thenReturn(volume);
    }

    private int callFetch() {
        return tickFetcher.fetch(fetchInstrument,
                                 startDate,
                                 endDate,
                                 tickMinutes,
                                 nTicks,
                                 tickParams);
    }

    private void setReturnedTickList(final List<ITick> tickList) {
        when(historyProviderMock.fetchTicks(Instrument.EURUSD,
                                            DateTimeUtils.millisFromOLEDate(startDate),
                                            DateTimeUtils.millisFromOLEDate(endDate)))
                                                .thenReturn(tickList);
    }

    @Test
    public void whenHistoryProviderReturnesEmptyListTheResultIsHistoryUnavailable() {
        setReturnedTickList(new ArrayList<>());

        assertThat(callFetch(), equalTo(ZorroReturnValues.HISTORY_UNAVAILABLE.getValue()));
    }

    public class WhenHistoryProviderReturnsTickMockList {

        private int returnValue;

        @Before
        public void setUp() {
            setReturnedTickList(tickMockList);

            returnValue = callFetch();
        }

        @Test
        public void returnedTickSizeIsTwo() {
            assertThat(returnValue, equalTo(2));
        }

        @Test
        public void fetchWasCalledCorrectOnHistoryProvider() {
            verify(historyProviderMock).fetchTicks(Instrument.EURUSD,
                                                   DateTimeUtils.millisFromOLEDate(startDate),
                                                   DateTimeUtils.millisFromOLEDate(endDate));
        }

        @Test
        public void filledTickValuesAreCorrect() {
            assertThat(tickParams[0], equalTo(tickBAsk));
            assertThat(tickParams[1], equalTo(tickBAsk));
            assertThat(tickParams[2], equalTo(tickBAsk));
            assertThat(tickParams[3], equalTo(tickBAsk));
            assertThat(tickParams[4], equalTo(DateTimeUtils.getUTCTimeFromTick(tickBMock)));
            assertThat(tickParams[5], equalTo(MathUtil.roundPrice(tickBAsk - tickBBid, fetchInstrument)));
            assertThat(tickParams[6], equalTo(tickBVol));

            assertThat(tickParams[7], equalTo(tickAAsk));
            assertThat(tickParams[8], equalTo(tickAAsk));
            assertThat(tickParams[9], equalTo(tickAAsk));
            assertThat(tickParams[10], equalTo(tickAAsk));
            assertThat(tickParams[11], equalTo(DateTimeUtils.getUTCTimeFromTick(tickAMock)));
            assertThat(tickParams[12], equalTo(MathUtil.roundPrice(tickAAsk - tickABid, fetchInstrument)));
            assertThat(tickParams[13], equalTo(tickAVol));
        }
    }
}
