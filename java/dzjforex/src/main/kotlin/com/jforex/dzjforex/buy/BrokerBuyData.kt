package com.jforex.dzjforex.buy

data class BrokerBuyData(
    val returnCode: Int,
    val price: Double = 0.0,
    val fill: Double = 0.0
)