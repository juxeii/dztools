package com.jforex.dzjforex.command

data class BrokerCommandData<T>(
    val returnCode: Int,
    val data: T
)