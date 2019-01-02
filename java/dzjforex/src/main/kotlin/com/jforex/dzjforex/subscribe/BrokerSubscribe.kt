package com.jforex.dzjforex.subscribe

import arrow.Kind
import arrow.effects.ForIO
import arrow.typeclasses.binding
import com.dukascopy.api.Instrument
import com.jforex.dzjforex.history.HistoryApi.fetchLatestQuote
import com.jforex.dzjforex.misc.*
import com.jforex.dzjforex.misc.ContextApi.getSubscribedInstruments
import com.jforex.dzjforex.misc.ContextApi.setSubscribedInstruments
import com.jforex.dzjforex.misc.InstrumentApi.fromAssetName
import com.jforex.dzjforex.quote.QuotesProviderApi.hasQuote
import com.jforex.dzjforex.quote.saveQuote
import com.jforex.dzjforex.zorro.SUBSCRIBE_FAIL
import com.jforex.dzjforex.zorro.SUBSCRIBE_OK
import com.jforex.kforexutils.instrument.InstrumentFactory
import com.jforex.kforexutils.instrument.currencies

fun createBrokerSubscribeApi(): QuoteDependencies<ForIO> = createQuoteApi(contextApi.jfContext)

object BrokerSubscribeApi
{
    fun <F> QuoteDependencies<F>.brokerSubscribe(assetName: String) =
        fromAssetName(assetName)
            .flatMap { instrument ->
                logger.debug("Subscribing asset $assetName")
                getInstrumentWithCrosses(instrument)
            }
            .flatMap { instrumentsToSubscribe -> setSubscribedInstruments(instrumentsToSubscribe) }
            .flatMap { fetchLatestQuotes() }
            .map {
                logger.debug(
                    "Successfully subscribed asset $assetName." +
                            " All subscribed assets: ${jfContext.subscribedInstruments}"
                )
                SUBSCRIBE_OK
            }
            .handleError { error ->
                logger.debug(
                    "BrokerSubscribe failed! " +
                            "Error message: ${error.message} " +
                            "Stack trace: ${getStackTrace(error)}"
                )
                SUBSCRIBE_FAIL
            }

    fun <F> QuoteDependencies<F>.getInstrumentWithCrosses(instrument: Instrument) = invoke {
        val accountCurrency = account.accountCurrency
        val currencies = instrument.currencies + accountCurrency
        InstrumentFactory.fromCombinedCurrencies(currencies)
    }

    fun <F> QuoteDependencies<F>.fetchLatestQuotes(): Kind<F, Unit> = binding {
        getSubscribedInstruments()
            .bind()
            .forEach { instrument ->
                if (!hasQuote(instrument))
                {
                    logger.debug("Quote for $instrument is not available in quoteProvider, looking up in history")
                    val quoteFromHistory = fetchLatestQuote(instrument).bind()
                    saveQuote(quoteFromHistory)
                }
            }
    }
}
