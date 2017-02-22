package com.jforex.dzjforex.config.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.jforex.dzjforex.config.ZorroReturnValues;

public class ZorroReturnValuesTest {

    private void assertEnumValue(final int value,
                                 final int expectedValue) {
        assertThat(value, equalTo(expectedValue));
    }

    @Test
    public void loginFAILIsZero() {
        assertEnumValue(ZorroReturnValues.LOGIN_FAIL.getValue(), 0);
    }

    @Test
    public void loginOKIsOne() {
        assertEnumValue(ZorroReturnValues.LOGIN_OK.getValue(), 1);
    }

    @Test
    public void logoutIsOne() {
        assertEnumValue(ZorroReturnValues.LOGOUT_OK.getValue(), 1);
    }

    @Test
    public void onConnectionLostNewLoginRequiredIsZero() {
        assertEnumValue(ZorroReturnValues.CONNECTION_LOST_NEW_LOGIN_REQUIRED.getValue(), 0);
    }

    @Test
    public void onMarketIsClosedIsOne() {
        assertEnumValue(ZorroReturnValues.CONNECTION_OK_BUT_MARKET_CLOSED.getValue(), 1);
    }

    @Test
    public void onConnectionOKIsTwo() {
        assertEnumValue(ZorroReturnValues.CONNECTION_OK.getValue(), 2);
    }

    @Test
    public void assetUnavailableIsZero() {
        assertEnumValue(ZorroReturnValues.ASSET_UNAVAILABLE.getValue(), 0);
    }

    @Test
    public void assetAvailableIsOne() {
        assertEnumValue(ZorroReturnValues.ASSET_AVAILABLE.getValue(), 1);
    }

    @Test
    public void accountUnavailableIsZero() {
        assertEnumValue(ZorroReturnValues.ACCOUNT_UNAVAILABLE.getValue(), 0);
    }

    @Test
    public void accountAvailableIsOne() {
        assertEnumValue(ZorroReturnValues.ACCOUNT_AVAILABLE.getValue(), 1);
    }

    @Test
    public void brokerBuyFailIsZero() {
        assertEnumValue(ZorroReturnValues.BROKER_BUY_FAIL.getValue(), 0);
    }

    @Test
    public void brokerBuyOppositeCloseIsOne() {
        assertEnumValue(ZorroReturnValues.BROKER_BUY_OPPOSITE_CLOSE.getValue(), 1);
    }

    @Test
    public void unknowndOrderIDIsZero() {
        assertEnumValue(ZorroReturnValues.UNKNOWN_ORDER_ID.getValue(), 0);
    }

    @Test
    public void orderRecentlyClosedIsNegative() {
        assertEnumValue(ZorroReturnValues.ORDER_RECENTLY_CLOSED.getValue(), -1);
    }

    @Test
    public void adjustSLOKIsPositive() {
        assertEnumValue(ZorroReturnValues.ADJUST_SL_OK.getValue(), 1);
    }

    @Test
    public void closingOrderFAILIsZero() {
        assertEnumValue(ZorroReturnValues.ADJUST_SL_FAIL.getValue(), 0);
    }

    @Test
    public void historyUnavailableIsZero() {
        assertEnumValue(ZorroReturnValues.HISTORY_UNAVAILABLE.getValue(), 0);
    }

    @Test
    public void historyDownloadFAILIsZero() {
        assertEnumValue(ZorroReturnValues.HISTORY_DOWNLOAD_FAIL.getValue(), 0);
    }

    @Test
    public void historyDownloadOKIsOne() {
        assertEnumValue(ZorroReturnValues.HISTORY_DOWNLOAD_OK.getValue(), 1);
    }

    @Test
    public void brokerCommandOKIsOne() {
        assertEnumValue(ZorroReturnValues.BROKER_COMMAND_OK.getValue(), 1);
    }

    @Test
    public void invalidServerTimeIsZero() {
        assertEnumValue(ZorroReturnValues.INVALID_SERVER_TIME.getValue(), 0);
    }
}
