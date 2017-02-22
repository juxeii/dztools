package com.jforex.dzjforex.config;

public enum ZorroReturnValues {

    LOGIN_FAIL(0),
    LOGIN_OK(1),
    LOGOUT_OK(1),

    CONNECTION_LOST_NEW_LOGIN_REQUIRED(0),
    CONNECTION_OK_BUT_MARKET_CLOSED(1),
    CONNECTION_OK(2),

    ASSET_UNAVAILABLE(0),
    ASSET_AVAILABLE(1),

    ACCOUNT_UNAVAILABLE(0),
    ACCOUNT_AVAILABLE(1),

    BROKER_BUY_FAIL(0),
    BROKER_BUY_OPPOSITE_CLOSE(1),

    UNKNOWN_ORDER_ID(0),
    ORDER_RECENTLY_CLOSED(-1),

    ADJUST_SL_FAIL(0),
    ADJUST_SL_OK(1),

    BROKER_SELL_FAIL(0),

    HISTORY_UNAVAILABLE(0),
    HISTORY_DOWNLOAD_FAIL(0),
    HISTORY_DOWNLOAD_OK(1),

    BROKER_COMMAND_OK(1),
    INVALID_SERVER_TIME(0);

    private final int value;

    ZorroReturnValues(final int newValue) {
        value = newValue;
    }

    public int getValue() {
        return value;
    }
}
