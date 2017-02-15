package com.jforex.dzjforex.settings;

public class ReturnCodes {

    public static final int LOGIN_FAIL = 0;
    public static final int LOGIN_OK = 1;
    public static final int LOGOUT_OK = 1;

    public static final int CONNECTION_LOST_NEW_LOGIN_REQUIRED = 0;
    public static final int CONNECTION_OK_BUT_MARKET_CLOSED = 1;
    public static final int CONNECTION_OK = 2;

    public static final int ASSET_UNAVAILABLE = 0;
    public static final int ASSET_AVAILABLE = 1;

    public static final int ACCOUNT_UNAVAILABLE = 0;
    public static final int ACCOUNT_AVAILABLE = 1;

    public static final int BROKER_BUY_FAIL = 0;
    public static final int BROKER_BUY_OPPOSITE_CLOSE = 1;

    public static final int UNKNOWN_ORDER_ID = 0;
    public static final int ORDER_RECENTLY_CLOSED = -1;

    public static final int ADJUST_SL_OK = 1;

    public static final int BROKER_SELL_FAIL = 0;

    public static final int HISTORY_UNAVAILABLE = 0;
    public static final int HISTORY_DOWNLOAD_FAIL = 0;
    public static final int HISTORY_DOWNLOAD_OK = 1;
}
