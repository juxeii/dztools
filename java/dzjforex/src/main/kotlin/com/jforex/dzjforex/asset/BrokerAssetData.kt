package com.jforex.dzjforex.asset

data class BrokerAssetData(
    val returnCode: Int,
    val price: Double = 0.0,
    val spread: Double = 0.0,
    val volume: Double = 0.0,
    val pip: Double = 0.0,
    val pipCost: Double = 0.0,
    val lotAmount: Double = 0.0,
    val marginCost: Double = 0.0
)