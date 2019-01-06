package com.jforex.dzjforex.account

data class BrokerAccountData(
    val returnCode: Int,
    val balance: Double = 0.0,
    val tradeVal: Double = 0.0,
    val marginVal: Double = 0.0
)