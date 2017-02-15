package com.jforex.dzjforex.settings.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.jforex.dzjforex.settings.ReturnCodes;

public class ReturnCodesTest {

    private void assertCode(final int code,
                            final int expectedValue) {
        assertThat(code, equalTo(expectedValue));
    }

    @Test
    public void loginFAILIsZero() {
        assertCode(ReturnCodes.LOGIN_FAIL, 0);
    }

    @Test
    public void loginOKIsOne() {
        assertCode(ReturnCodes.LOGIN_OK, 1);
    }

    @Test
    public void logoutIsOne() {
        assertCode(ReturnCodes.LOGOUT_OK, 1);
    }

    @Test
    public void onConnectionLostNewLoginRequiredIsZero() {
        assertCode(ReturnCodes.CONNECTION_LOST_NEW_LOGIN_REQUIRED, 0);
    }

    @Test
    public void onMarketIsClosedIsOne() {
        assertCode(ReturnCodes.CONNECTION_OK_BUT_MARKET_CLOSED, 1);
    }

    @Test
    public void onConnectionOKIsTwo() {
        assertCode(ReturnCodes.CONNECTION_OK, 2);
    }

    @Test
    public void assetUnavailableIsZero() {
        assertCode(ReturnCodes.ASSET_UNAVAILABLE, 0);
    }

    @Test
    public void assetAvailableIsOne() {
        assertCode(ReturnCodes.ASSET_AVAILABLE, 1);
    }

    @Test
    public void accountUnavailableIsZero() {
        assertCode(ReturnCodes.ACCOUNT_UNAVAILABLE, 0);
    }

    @Test
    public void accountAvailableIsOne() {
        assertCode(ReturnCodes.ACCOUNT_AVAILABLE, 1);
    }

    @Test
    public void brokerBuyFailIsZero() {
        assertCode(ReturnCodes.BROKER_BUY_FAIL, 0);
    }

    @Test
    public void brokerBuyOppositeCloseIsOne() {
        assertCode(ReturnCodes.BROKER_BUY_OPPOSITE_CLOSE, 1);
    }

    @Test
    public void unknowndOrderIDIsZero() {
        assertCode(ReturnCodes.UNKNOWN_ORDER_ID, 0);
    }

    @Test
    public void orderRecentlyClosedIsNegative() {
        assertCode(ReturnCodes.ORDER_RECENTLY_CLOSED, -1);
    }

    @Test
    public void closingOrderFAILIsZero() {
        assertCode(ReturnCodes.ORDER_CLOSE_FAIL, 0);
    }

    @Test
    public void historyUnavailableIsZero() {
        assertCode(ReturnCodes.HISTORY_UNAVAILABLE, 0);
    }
}
