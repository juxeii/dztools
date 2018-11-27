package com.jforex.dzjforex.zorro

const val LOGIN_OK = 1
const val LOGIN_FAIL = 0
const val LOGOUT_OK = 0

const val ASSET_AVAILABLE = 1
const val ASSET_UNAVAILABLE = 0

const val CONNECTION_LOST_NEW_LOGIN_REQUIRED = 0
const val CONNECTION_OK_BUT_MARKET_CLOSED = 1
const val CONNECTION_OK_BUT_TRADING_NOT_ALLOWED = 1
const val CONNECTION_OK = 2

const val heartBeatIndication  = 1
const val demoLoginType = "Demo"
const val realLoginType = "Real"