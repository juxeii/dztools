package com.jforex.dzjforex.subscription

import com.dukascopy.api.Instrument
import com.jforex.kforexutils.price.TickQuote

data class InitialQuote(
    val instrument: Instrument,
    val quote: TickQuote
)