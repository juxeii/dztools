package com.jforex.dzjforex.config.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.jforex.dzjforex.config.Constant;

public class ConstantTest {

    private void assertConstantValue(final long value,
                                     final long expectedValue) {
        assertThat(value, equalTo(expectedValue));
    }

    @Test
    public void loginFAILIsZero() {
        assertConstantValue(Constant.LOGIN_FAIL, 0);
    }

    @Test
    public void loginOKIsOne() {
        assertConstantValue(Constant.LOGIN_OK, 1);
    }

    @Test
    public void logoutIsOne() {
        assertConstantValue(Constant.LOGOUT_OK, 1);
    }

    @Test
    public void onConnectionLostNewLoginRequiredIsZero() {
        assertConstantValue(Constant.CONNECTION_LOST_NEW_LOGIN_REQUIRED, 0);
    }

    @Test
    public void onMarketIsClosedIsOne() {
        assertConstantValue(Constant.CONNECTION_OK_BUT_MARKET_CLOSED, 1);
    }

    @Test
    public void onConnectionOKIsTwo() {
        assertConstantValue(Constant.CONNECTION_OK, 2);
    }

    @Test
    public void assetUnavailableIsZero() {
        assertConstantValue(Constant.ASSET_UNAVAILABLE, 0);
    }

    @Test
    public void assetAvailableIsOne() {
        assertConstantValue(Constant.ASSET_AVAILABLE, 1);
    }

    @Test
    public void accountUnavailableIsZero() {
        assertConstantValue(Constant.ACCOUNT_UNAVAILABLE, 0);
    }

    @Test
    public void accountAvailableIsOne() {
        assertConstantValue(Constant.ACCOUNT_AVAILABLE, 1);
    }

    @Test
    public void brokerBuyFailIsZero() {
        assertConstantValue(Constant.BROKER_BUY_FAIL, 0);
    }

    @Test
    public void brokerBuyOppositeCloseIsOne() {
        assertConstantValue(Constant.BROKER_BUY_OPPOSITE_CLOSE, 1);
    }

    @Test
    public void unknowndOrderIDIsZero() {
        assertConstantValue(Constant.UNKNOWN_ORDER_ID, 0);
    }

    @Test
    public void orderRecentlyClosedIsNegative() {
        assertConstantValue(Constant.ORDER_RECENTLY_CLOSED, -1);
    }

    @Test
    public void adjustSLOKIsPositive() {
        assertConstantValue(Constant.ADJUST_SL_OK, 1);
    }

    @Test
    public void closingOrderFAILIsZero() {
        assertConstantValue(Constant.BROKER_SELL_FAIL, 0);
    }

    @Test
    public void historyUnavailableIsZero() {
        assertConstantValue(Constant.HISTORY_UNAVAILABLE, 0);
    }

    @Test
    public void historyDownloadFAILIsZero() {
        assertConstantValue(Constant.HISTORY_DOWNLOAD_FAIL, 0);
    }

    @Test
    public void historyDownloadOKIsOne() {
        assertConstantValue(Constant.HISTORY_DOWNLOAD_OK, 1);
    }

    @Test
    public void brokerCommandOKIsOne() {
        assertConstantValue(Constant.BROKER_COMMAND_OK, 1);
    }

    @Test
    public void invalidServerTimeIsZero() {
        assertConstantValue(Constant.INVALID_SERVER_TIME, 0L);
    }
}
