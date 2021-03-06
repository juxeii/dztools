package com.jforex.dzjforex.zorro

const val LOGIN_OK = 1
const val LOGIN_FAIL = 0
const val LOGOUT_OK = 0

const val SUBSCRIBE_OK = 1
const val SUBSCRIBE_FAIL = 0

const val ASSET_AVAILABLE = 1
const val ASSET_UNAVAILABLE = 0

const val CONNECTION_LOST_NEW_LOGIN_REQUIRED = 0
const val CONNECTION_OK_BUT_MARKET_CLOSED = 1
const val CONNECTION_OK_BUT_TRADING_NOT_ALLOWED = 1
const val CONNECTION_OK = 2

const val ACCOUNT_UNAVAILABLE = 0
const val ACCOUNT_AVAILABLE = 1

const val BROKER_BUY_FAIL = 0
const val BROKER_BUY_FILL_TIMEOUT = 0
const val BROKER_BUY_NO_RESPONSE = -2
const val BROKER_BUY_OPPOSITE_CLOSE = 1

const val BROKER_SELL_FAIL = 0
const val BROKER_ORDER_NOT_YET_FILLED = 0

const val BROKER_TRADE_FAIL = 0

const val BROKER_HISTORY_UNAVAILABLE = 0

const val BROKER_ADJUST_SL_FAIL = 0
const val BROKER_ADJUST_SL_OK = 1

const val heartBeatIndication = 1
const val demoLoginType = "Demo"
const val realLoginType = "Real"
const val lotScale = 1000000.0
const val fillTimeout = 30L
const val tickPeriod = 0

const val BROKER_COMMAND_OK = 1.0
const val BROKER_COMMAND_ERROR = 0.0