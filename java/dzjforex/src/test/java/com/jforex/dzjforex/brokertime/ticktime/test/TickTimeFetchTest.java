package com.jforex.dzjforex.brokertime.ticktime.test;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.google.common.collect.Maps;
import com.jforex.dzjforex.brokertime.ticktime.TickTimeFetch;
import com.jforex.dzjforex.testutil.BarsAndTicksForTest;
import com.jforex.programming.quote.TickQuote;
import com.jforex.programming.quote.TickQuoteRepository;

import io.reactivex.observers.TestObserver;

public class TickTimeFetchTest extends BarsAndTicksForTest {

    private TickTimeFetch tickHistoryByShift;

    @Mock
    private TickQuoteRepository tickQuoteRepositoryMock;
    private final Map<Instrument, TickQuote> quotesMap = Maps.newHashMap();

    @Before
    public void setUp() {
        when(tickQuoteRepositoryMock.getAll()).thenReturn(quotesMap);

        tickHistoryByShift = new TickTimeFetch(tickQuoteRepositoryMock);
    }

    private TestObserver<Long> subscribe() {
        return tickHistoryByShift
            .get()
            .test();
    }

    @Test
    public void getCallIsDeferred() {
        tickHistoryByShift.get();

        verifyZeroInteractions(tickQuoteRepositoryMock);
    }

    @Test
    public void whenQuotesMapIsEmptyErrorIsPropagated() {
        subscribe().assertError(JFException.class);
    }

    @Test
    public void whenQuotesMapIsNotEmptyMaxTickTimeIsReturned() {
        quotesMap.put(instrumentForTest, tickQuoteA);
        quotesMap.put(instrumentForTest, tickQuoteB);
        quotesMap.put(instrumentForTest, tickQuoteC);

        subscribe().assertValue(tickCTime);
    }
}
