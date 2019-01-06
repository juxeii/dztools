package com.jforex.dzjforex.history

data class BrokerHistoryData(
    val returnCode: Int,
    val ticks: List<T6Data> = emptyList()
)