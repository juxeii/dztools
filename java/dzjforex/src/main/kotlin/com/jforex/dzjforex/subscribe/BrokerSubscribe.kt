package com.jforex.dzjforex.subscribe

import arrow.Kind
import arrow.effects.ForIO
import arrow.typeclasses.binding
import com.dukascopy.api.Instrument
import com.jforex.dzjforex.history.HistoryApi.waitForLatestQuote
import com.jforex.dzjforex.misc.*
import com.jforex.dzjforex.misc.ContextApi.getSubscribedInstruments
import com.jforex.dzjforex.misc.InstrumentApi.fromAssetName
import com.jforex.dzjforex.misc.QuotesProviderApi.hasQuote
import com.jforex.dzjforex.zorro.SUBSCRIBE_FAIL
import com.jforex.dzjforex.zorro.SUBSCRIBE_OK
import com.jforex.kforexutils.instrument.InstrumentFactory
import com.jforex.kforexutils.instrument.currencies

fun createBrokerSubscribeApi(): QuoteDependencies<ForIO> = createQuoteApi(contextApi.context)

object BrokerSubscribeApi
{
    fun <F> QuoteDependencies<F>.brokerSubscribe(assetName: String): Kind<F, Int> =
        fromAssetName(assetName)
            .flatMap { instrument ->
                logger.debug("Subscribed are ${context.subscribedInstruments}")
                getInstrumentWithCrosses(instrument)
            }
            .flatMap { instrumentsToSubscribe -> setSubscribedInstruments(instrumentsToSubscribe) }
            .flatMap { waitForLatestQuotes() }
            .map { SUBSCRIBE_OK }
            .handleError { error ->
                logger.debug("BrokerSubscribe failed! Error: $error Stack trace: ${getStackTrace(error)}")
                SUBSCRIBE_FAIL
            }

    fun <F> QuoteDependencies<F>.getInstrumentWithCrosses(instrument: Instrument): Kind<F, Set<Instrument>>
    {
        val accountCurrency = account.accountCurrency
        val currencies = instrument.currencies + accountCurrency
        return just(InstrumentFactory.fromCombinedCurrencies(currencies))
    }

    fun <F> QuoteDependencies<F>.waitForLatestQuotes(): Kind<F, Unit> =
        binding {
            getSubscribedInstruments()
                .bind()
                .forEach { instrument ->
                    if (!hasQuote(instrument))
                    {
                        logger.debug("Quote for $instrument is not available in quoteProvider, looking up in history")
                        val quoteFromHistory = waitForLatestQuote(instrument).bind()
                        saveQuote(quoteFromHistory)
                    }
                }
        }

    fun <F> QuoteDependencies<F>.setSubscribedInstruments(instrumentsToSubscribe: Set<Instrument>): Kind<F, Unit> =
        invoke {
            val subscribedInstruments = context.subscribedInstruments
            context.setSubscribedInstruments(subscribedInstruments + instrumentsToSubscribe, false)
        }
}
