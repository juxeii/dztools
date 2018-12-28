package com.jforex.dzjforex.subscription

import arrow.Kind
import arrow.effects.ForIO
import arrow.effects.IO
import arrow.effects.instances.io.monadError.monadError
import arrow.typeclasses.binding
import com.dukascopy.api.Instrument
import com.jforex.dzjforex.misc.ContextApi.getSubscribedInstruments
import com.jforex.dzjforex.history.HistoryApi.waitForLatestQuote
import com.jforex.dzjforex.misc.*
import com.jforex.dzjforex.misc.InstrumentApi.fromAssetName
import com.jforex.dzjforex.misc.QuotesProviderApi.getQuote
import com.jforex.dzjforex.misc.QuotesProviderApi.hasQuote
import com.jforex.dzjforex.zorro.SUBSCRIBE_FAIL
import com.jforex.dzjforex.zorro.SUBSCRIBE_OK
import com.jforex.kforexutils.instrument.InstrumentFactory
import com.jforex.kforexutils.instrument.currencies
import com.jforex.kforexutils.price.TickQuote

fun createBrokerSubscribeApi(): QuoteDependencies<ForIO> = createQuoteApi(contextApi.context, IO.monadError())

object BrokerSubscribeApi
{
    fun <F> QuoteDependencies<F>.subscribeInstrument(assetName: String): Kind<F, Int> =
        binding {
            val instrument = fromAssetName(assetName).bind()
            logger.debug("BrokerSubscribe $instrument pC ${instrument.primaryJFCurrency}" +
                    " sC ${instrument.secondaryJFCurrency} minLot ${instrument.minTradeAmount}" +
                    " type ${instrument.type} pipValue ${instrument.pipValue}")
            val instrumentsToSubscribe = getInstrumentsToSubscribe(instrument).bind()
            subscribeAllInstruments(instrumentsToSubscribe).bind()
            logger.debug("BrokerSubscribe instrumentsToSubscribe $instrumentsToSubscribe")
            val latestQuotes = waitForLatestQuotes(instrumentsToSubscribe).bind()
            latestQuotes.forEach { quote -> saveQuote(quote) }
            SUBSCRIBE_OK
        }.handleError { SUBSCRIBE_FAIL }

    fun <F> QuoteDependencies<F>.getInstrumentWithCrosses(instrument: Instrument): Kind<F, Set<Instrument>>
    {
        val accountCurrency = account.accountCurrency
        val currencies = instrument.currencies + accountCurrency
        return just(InstrumentFactory.fromCombinedCurrencies(currencies))
    }

    fun <F> QuoteDependencies<F>.getInstrumentsToSubscribe(instrument: Instrument): Kind<F, Set<Instrument>> =
        binding {
            val instrumentWithCrosses = getInstrumentWithCrosses(instrument).bind()
            val subscribedInstruments = getSubscribedInstruments().bind()
            instrumentWithCrosses - subscribedInstruments
        }

    fun <F> QuoteDependencies<F>.subscribeAllInstruments(instrumentsToSubscribe: Set<Instrument>)
            : Kind<F, Unit> = binding {
        val subscribedInstruments = getSubscribedInstruments().bind()
        val resultingInstruments = subscribedInstruments + instrumentsToSubscribe
        setSubscribedInstruments(resultingInstruments).bind()
    }

    fun <F> QuoteDependencies<F>.waitForLatestQuotes(instruments: Set<Instrument>): Kind<F, List<TickQuote>> =
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

    fun <F> QuoteDependencies<F>.setSubscribedInstruments(instrumentsToSubscribe: Set<Instrument>): Kind<F, Unit> =
        just(context.setSubscribedInstruments(instrumentsToSubscribe, true))
}
