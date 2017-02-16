package com.jforex.dzjforex.brokerapi.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.system.IClient;
import com.jforex.dzjforex.brokerapi.BrokerTime;
import com.jforex.dzjforex.config.ReturnCodes;
import com.jforex.dzjforex.datetime.DateTimeUtils;
import com.jforex.dzjforex.test.util.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;

@RunWith(HierarchicalContextRunner.class)
public class BrokerTimeTest extends CommonUtilForTest {

    private BrokerTime brokerTime;

    @Mock
    private IClient client;
    @Mock
    private DateTimeUtils dateTimeUtils;
    private final double serverTimeParams[] = new double[1];
    private int returnCode;

    private static final long serverTime = 1234567L;

    @Before
    public void setUp() {
        when(dateTimeUtils.getServerTime()).thenReturn(serverTime);
//        when(accountInfo.tradeValue()).thenReturn(tradeValue);
//        when(accountInfo.usedMargin()).thenReturn(usedMargin);

        brokerTime = new BrokerTime(client, dateTimeUtils);
    }

    private void setClientConnectivity(final boolean isClientConnected) {
        when(client.isConnected()).thenReturn(isClientConnected);
    }

    private void assertReturnCode(final int expectedReturnCode) {
        assertThat(returnCode, equalTo(expectedReturnCode));
    }

    private void assertServerTimeWasSetCorrect() {
        assertThat(serverTimeParams[0], equalTo(DateTimeUtils.getOLEDateFromMillis(serverTime)));
    }

    public class TestWhenClientIsDisconnected {

        @Before
        public void setUp() {
            setClientConnectivity(false);
            returnCode = brokerTime.handle(serverTimeParams);
        }

        @Test
        public void returnCodeIndicatesNewLoginRequired() {
            assertReturnCode(ReturnCodes.CONNECTION_LOST_NEW_LOGIN_REQUIRED);
        }

        @Test
        public void noServerTimeWasSet() {
            assertThat(serverTimeParams[0], equalTo(0.0));
        }
    }

    public class TestWhenClientIsConnected {

        private void setMarketConnectivityAndStartHandle(final boolean isMarketOffline) {
            when(dateTimeUtils.isMarketOffline(anyLong())).thenReturn(isMarketOffline);
            returnCode = brokerTime.handle(serverTimeParams);
        }

        @Before
        public void setUp() {
            when(client.isConnected()).thenReturn(true);
        }

        public class WhenMarketIsOpen {

            @Before
            public void setUp() {
                setMarketConnectivityAndStartHandle(false);
            }

            @Test
            public void serverTimeWasSetCorrect() {
                assertServerTimeWasSetCorrect();
            }

            @Test
            public void returnCodeIsConnected() {
                assertReturnCode(ReturnCodes.CONNECTION_OK);
            }
        }

        public class WhenMarketIsOffline {

            @Before
            public void setUp() {
                setMarketConnectivityAndStartHandle(true);
            }

            @Test
            public void returnCodeIsConnectedButMarketClosed() {
                assertReturnCode(ReturnCodes.CONNECTION_OK_BUT_MARKET_CLOSED);
            }

            @Test
            public void serverTimeWasSetCorrect() {
                assertServerTimeWasSetCorrect();
            }
        }
    }
}
