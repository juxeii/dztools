package com.jforex.dzjforex.brokerapi.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.brokerapi.BrokerHistory2;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.history.BarFetcher;
import com.jforex.dzjforex.history.TickFetcher;
import com.jforex.dzjforex.test.util.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;

@RunWith(HierarchicalContextRunner.class)
public class BrokerHistory2Test extends CommonUtilForTest {

    private BrokerHistory2 brokerHistory2;

    @Mock
    private BarFetcher barFetcherMock;
    @Mock
    private TickFetcher tickFetcherMock;
    private String assetName;
    private final double startDate = 12.4;
    private final double endDate = 13.7;
    private int tickMinutes;
    private final int nTicks = 270;
    private final double tickParams[] = new double[7];

    @Before
    public void setUp() {
        brokerHistory2 = new BrokerHistory2(barFetcherMock, tickFetcherMock);
    }

    private int callGet() {
        return brokerHistory2.get(assetName,
                                  startDate,
                                  endDate,
                                  tickMinutes,
                                  nTicks,
                                  tickParams);
    }

    @Test
    public void anInvalidAssetNameGivesHistoryUnavailable() {
        assetName = "Invalid";

        assertThat(callGet(), equalTo(ZorroReturnValues.HISTORY_UNAVAILABLE.getValue()));
    }

    public class WhenAssetNameIsValid {

        @Before
        public void setUp() {
            assetName = "EUR/USD";
        }

        @Test
        public void barFetcherIsCalledWhenTickMinutesAreNotZero() {
            tickMinutes = 1;
            final int expectedNoOfTicksReturned = 37;
            when(barFetcherMock.fetch(Instrument.EURUSD,
                                      startDate,
                                      endDate,
                                      tickMinutes,
                                      nTicks,
                                      tickParams))
                                          .thenReturn(expectedNoOfTicksReturned);

            final int noOfTicks = callGet();

            assertThat(noOfTicks, equalTo(expectedNoOfTicksReturned));
            verify(barFetcherMock).fetch(Instrument.EURUSD,
                                         startDate,
                                         endDate,
                                         tickMinutes,
                                         nTicks,
                                         tickParams);
        }

        @Test
        public void tickFetcherIsCalledWhenTickMinutesAreZero() {
            tickMinutes = 0;
            final int expectedNoOfTicksReturned = 42;
            when(tickFetcherMock.fetch(Instrument.EURUSD,
                                       startDate,
                                       endDate,
                                       tickMinutes,
                                       nTicks,
                                       tickParams))
                                           .thenReturn(expectedNoOfTicksReturned);

            final int noOfTicks = callGet();

            assertThat(noOfTicks, equalTo(expectedNoOfTicksReturned));
            verify(tickFetcherMock).fetch(Instrument.EURUSD,
                                          startDate,
                                          endDate,
                                          tickMinutes,
                                          nTicks,
                                          tickParams);
        }
    }
}
