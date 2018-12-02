package com.jforex.dzjforex.login

import arrow.core.Option

internal data class BrokerLoginResult(
    val callResult: Int,
    val maybeAccountName: Option<String> = Option.empty()
)