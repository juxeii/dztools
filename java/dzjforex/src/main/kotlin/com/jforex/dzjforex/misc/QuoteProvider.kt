package com.jforex.dzjforex.misc

import com.dukascopy.api.Instrument
import com.jforex.kforexutils.misc.KForexUtils
import com.jforex.kforexutils.price.TickQuote
import io.reactivex.rxkotlin.subscribeBy

class QuoteProvider(private val kForexUtils: KForexUtils) {

    private var latestTicks: MutableMap<Instrument, TickQuote> = mutableMapOf()

    init {
        kForexUtils
            .tickQuotes
            .subscribeBy(onNext = { latestTicks[it.instrument] = it })
    }
}