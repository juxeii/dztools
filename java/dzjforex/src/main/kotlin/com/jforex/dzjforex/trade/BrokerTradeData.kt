package com.jforex.dzjforex.trade

data class BrokerTradeData(
    val returnCode: Int,
    val open: Double = 0.0,
    val close: Double = 0.0,
    val profit: Double = 0.0
)