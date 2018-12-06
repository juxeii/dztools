package com.jforex.dzjforex.subscription

data class SubscribeResult(
    val state: Int,
    val initialQuotes: List<InitialQuote>
)