package com.jforex.dzjforex.subscription

import arrow.Kind
import arrow.effects.ForIO
import arrow.typeclasses.binding
import com.dukascopy.api.Instrument
import com.jforex.dzjforex.history.HistoryApi.waitForLatestQuote
import com.jforex.dzjforex.history.historyApi
import com.jforex.dzjforex.misc.ContextDependencies
import com.jforex.dzjforex.misc.InstrumentApi.forexInstrumentFromAssetName
import com.jforex.dzjforex.misc.QuoteProviderDependencies
import com.jforex.dzjforex.misc.QuotesApi.getQuote
import com.jforex.dzjforex.misc.QuotesApi.hasQuote
import com.jforex.dzjforex.misc.createQuoteProviderApi
import com.jforex.dzjforex.misc.saveQuote
import com.jforex.dzjforex.zorro.SUBSCRIBE_FAIL
import com.jforex.dzjforex.zorro.SUBSCRIBE_OK
import com.jforex.kforexutils.instrument.InstrumentFactory
import com.jforex.kforexutils.instrument.currencies
import com.jforex.kforexutils.price.TickQuote
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

private val logger: Logger = LogManager.getLogger()

fun createBrokerSubscribeApi(): BrokerSubscribeDependencies<ForIO> =
    BrokerSubscribeDependencies(historyApi, createQuoteProviderApi())

interface BrokerSubscribeDependencies<F> : ContextDependencies<F>,
    QuoteProviderDependencies
{
    companion object
    {
        operator fun <F> invoke(
            contextDependencies: ContextDependencies<F>,
            quoteProviderDependencies: QuoteProviderDependencies
        ): BrokerSubscribeDependencies<F> =
            object : BrokerSubscribeDependencies<F>,
                ContextDependencies<F> by contextDependencies,
                QuoteProviderDependencies by quoteProviderDependencies
            {}
    }
}

object BrokerSubscribeApi
{
    fun <F> BrokerSubscribeDependencies<F>.subscribeInstrument(assetName: String): Kind<F, Int> =
        binding {
            val instrument = forexInstrumentFromAssetName(assetName).bind()
            val instrumentsToSubscribe = getInstrumentsToSubscribe(instrument).bind()
            val resultingInstruments = subscribeAllInstruments(instrumentsToSubscribe).bind()
            val latestQuotes = waitForLatestQuotes(resultingInstruments).bind()
            latestQuotes.forEach { quote -> saveQuote(quote) }
            SUBSCRIBE_OK
        }.handleError { SUBSCRIBE_FAIL }

    fun <F> BrokerSubscribeDependencies<F>.getInstrumentWithCrosses(instrument: Instrument): Kind<F, Set<Instrument>>
    {
        val accountCurrency = account.accountCurrency
        val currencies = instrument.currencies + accountCurrency
        return just(InstrumentFactory.fromCombinedCurrencies(currencies))
    }

    fun <F> BrokerSubscribeDependencies<F>.getInstrumentsToSubscribe(instrument: Instrument): Kind<F, Set<Instrument>> =
        binding {
            val instrumentWithCrosses = getInstrumentWithCrosses(instrument).bind()
            val subscribedInstruments = getSubscribedInstruments()
            instrumentWithCrosses - subscribedInstruments
        }

    fun <F> BrokerSubscribeDependencies<F>.subscribeAllInstruments(instrumentsToSubscribe: Set<Instrument>)
            : Kind<F, Set<Instrument>> = binding {
        val subscribedInstruments = getSubscribedInstruments()
        val resultingInstruments = subscribedInstruments + instrumentsToSubscribe
        setSubscribedInstruments(resultingInstruments)
        resultingInstruments
    }

    fun <F> BrokerSubscribeDependencies<F>.waitForLatestQuotes(instruments: Set<Instrument>): Kind<F, List<TickQuote>> =
        binding {
            val quotes = mutableListOf<TickQuote>()
            instruments.forEach { instrument ->
                if (hasQuote(instrument))
                {
                    logger.debug("Quote for $instrument is available in quoteProvider")
                    quotes.add(getQuote(instrument))
                } else
                {
                    logger.debug("Quote for $instrument is not available in quoteProvider, looking up in history")
                    quotes.add(waitForLatestQuote(instrument).bind())
                }
            }
            quotes
        }

    fun <F> BrokerSubscribeDependencies<F>.getSubscribedInstruments() = context.subscribedInstruments

    fun <F> BrokerSubscribeDependencies<F>.setSubscribedInstruments(instrumentsToSubscribe: Set<Instrument>)
    {
        context.setSubscribedInstruments(instrumentsToSubscribe, true)
    }
}
