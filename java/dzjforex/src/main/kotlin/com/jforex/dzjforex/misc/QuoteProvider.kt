package com.jforex.dzjforex.misc

import arrow.core.toT
import arrow.data.State
import com.dukascopy.api.Instrument
import com.jforex.kforexutils.price.TickQuote
import org.apache.logging.log4j.LogManager

private val logger = LogManager.getLogger()

typealias Quotes = Map<Instrument, TickQuote>

interface QuoteProviderDependencies
{
    val quotes: Quotes

    companion object
    {
        operator fun invoke(quotes: Quotes): QuoteProviderDependencies =
            object : QuoteProviderDependencies
            {
                override val quotes = quotes
            }
    }
}

object QuotesApi
{

    fun QuoteProviderDependencies.hasQuote(instrument: Instrument) = quotes.containsKey(instrument)

    fun QuoteProviderDependencies.getQuote(instrument: Instrument) = quotes[instrument]!!

    fun QuoteProviderDependencies.getTick(instrument: Instrument) = getQuote(instrument).tick

    fun QuoteProviderDependencies.getAsk(instrument: Instrument) = getTick(instrument).ask

    fun QuoteProviderDependencies.getBid(instrument: Instrument) = getTick(instrument).bid

    fun QuoteProviderDependencies.getSpread(instrument: Instrument) = getBid(instrument) - getAsk(instrument)
}

fun updateQuotes(quote: TickQuote) = State<Quotes, Unit> {
    it.plus(Pair(quote.instrument, quote)) toT Unit
}