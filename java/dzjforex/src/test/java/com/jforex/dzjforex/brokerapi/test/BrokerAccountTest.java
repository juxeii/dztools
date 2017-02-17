package com.jforex.dzjforex.brokerapi.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.jforex.dzjforex.brokerapi.BrokerAccount;
import com.jforex.dzjforex.config.ReturnCodes;
import com.jforex.dzjforex.handler.AccountInfo;
import com.jforex.dzjforex.test.util.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;

@RunWith(HierarchicalContextRunner.class)
public class BrokerAccountTest extends CommonUtilForTest {

    private BrokerAccount brokerAccount;

    @Mock
    private AccountInfo accountInfo;
    private final double accountInfoParams[] = new double[3];
    private int returnCode;

    private static final double equity = 123.45;
    private static final double tradeValue = 65.45;
    private static final double usedMargin = 3.19;

    @Before
    public void setUp() {
        when(accountInfo.equity()).thenReturn(equity);
        when(accountInfo.tradeValue()).thenReturn(tradeValue);
        when(accountInfo.usedMargin()).thenReturn(usedMargin);

        brokerAccount = new BrokerAccount(accountInfo);
    }

    private void callHandleWithAccountConnectivity(final boolean isAccoutConnected) {
        when(accountInfo.isConnected()).thenReturn(isAccoutConnected);

        returnCode = brokerAccount.handle(accountInfoParams);
    }

    private void assertReturnCode(final int expectedReturnCode) {
        assertThat(returnCode, equalTo(expectedReturnCode));
    }

    public class TestWhenAccountIsDisconnected {

        @Before
        public void setUp() {
            callHandleWithAccountConnectivity(false);
        }

        @Test
        public void returnCodeIsUnavailable() {
            assertReturnCode(ReturnCodes.ACCOUNT_UNAVAILABLE);
        }

        @Test
        public void noAccountDataHaveBeenSet() {
            assertThat(accountInfoParams[0], equalTo(0.0));
            assertThat(accountInfoParams[1], equalTo(0.0));
            assertThat(accountInfoParams[2], equalTo(0.0));
        }
    }

    public class TestWhenAccountIsConnected {

        @Before
        public void setUp() {
            callHandleWithAccountConnectivity(true);
        }

        @Test
        public void returnCodeIsAvailable() {
            assertReturnCode(ReturnCodes.ACCOUNT_AVAILABLE);
        }

        @Test
        public void equityIsCorrectSet() {
            assertThat(accountInfoParams[0], equalTo(equity));
        }

        @Test
        public void tradeValueIsCorrectSet() {
            assertThat(accountInfoParams[1], equalTo(tradeValue));
        }

        @Test
        public void userMarginIsCorrectSet() {
            assertThat(accountInfoParams[2], equalTo(usedMargin));
        }
    }
}
