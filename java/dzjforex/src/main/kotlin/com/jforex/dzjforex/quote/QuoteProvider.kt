package com.jforex.dzjforex.quote

import com.dukascopy.api.Instrument
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jforex.kforexutils.price.Price
import com.jforex.kforexutils.price.TickQuote

typealias Quotes = Map<Instrument, TickQuote>

val quotesRelay: BehaviorRelay<Quotes> = BehaviorRelay.createDefault(emptyMap())

fun createQuoteProviderApi() =
    QuoteProviderDependencies(getQuotes())

fun saveQuote(quote: TickQuote)
{
    quotesRelay.accept(updateQuotes(quote))
}

fun updateQuotes(quote: TickQuote) = getQuotes().plus(Pair(quote.instrument, quote))

fun getQuotes() = quotesRelay.value!!

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

object QuotesProviderApi
{
    fun QuoteProviderDependencies.hasQuote(instrument: Instrument) = quotes.containsKey(instrument)

    fun QuoteProviderDependencies.getQuote(instrument: Instrument) = quotes[instrument]!!

    fun QuoteProviderDependencies.getTick(instrument: Instrument) = getQuote(instrument).tick

    fun QuoteProviderDependencies.getAsk(instrument: Instrument) = getTick(instrument).ask

    fun QuoteProviderDependencies.getBid(instrument: Instrument) = getTick(instrument).bid

    fun QuoteProviderDependencies.getSpread(instrument: Instrument) =
        Price(instrument, getAsk(instrument) - getBid(instrument)).toDouble()
}