package com.jforex.dzjforex.misc.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.jforex.dzjforex.misc.PriceProvider;
import com.jforex.dzjforex.test.util.CommonUtilForTest;
import com.jforex.programming.instrument.InstrumentUtil;

public class PriceProviderTest extends CommonUtilForTest {

    private PriceProvider priceProvider;

    @Mock
    private InstrumentUtil instrumentUtilMock;
    private final double ask = 1.12345;
    private final double bid = 1.12327;
    private final double spread = 1.8;

    @Before
    public void setUp() {
        when(strategyUtilMock.instrumentUtil(instrumentForTest))
            .thenReturn(instrumentUtilMock);

        priceProvider = new PriceProvider(strategyUtilMock);
    }

    @Test
    public void askPriceIsCorrect() {
        when(instrumentUtilMock.askQuote()).thenReturn(ask);

        assertThat(priceProvider.ask(instrumentForTest), equalTo(ask));
    }

    @Test
    public void bidPriceIsCorrect() {
        when(instrumentUtilMock.bidQuote()).thenReturn(bid);

        assertThat(priceProvider.bid(instrumentForTest), equalTo(bid));
    }

    @Test
    public void spreadIsCorrect() {
        when(instrumentUtilMock.spread()).thenReturn(spread);

        assertThat(priceProvider.spread(instrumentForTest), equalTo(spread));
    }
}
