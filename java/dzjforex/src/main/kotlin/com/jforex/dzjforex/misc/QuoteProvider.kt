package com.jforex.dzjforex.misc

import arrow.data.getOption
import com.dukascopy.api.Instrument
import com.jforex.kforexutils.misc.KForexUtils
import com.jforex.kforexutils.price.TickQuote
import io.reactivex.rxkotlin.subscribeBy
import org.apache.logging.log4j.LogManager

class QuoteProvider(private val kForexUtils: KForexUtils)
{
    private var latestTicks: MutableMap<Instrument, TickQuote> = mutableMapOf()
    private val logger = LogManager.getLogger(QuoteProvider::class.java)

    init
    {
        kForexUtils
            .tickQuotes
            .subscribeBy(onNext = {
                logger.debug("Received tick quote $it")
                latestTicks[it.instrument] = it
            })
    }

    private fun tick(instrument: Instrument) = latestTicks
        .getOption(instrument)
        .map { it.tick }

    fun ask(instrument: Instrument) = tick(instrument).map { it.ask }

    fun bid(instrument: Instrument) = tick(instrument).map { it.bid }

    fun spread(instrument: Instrument) = tick(instrument).map { it.bid - it.ask }
}