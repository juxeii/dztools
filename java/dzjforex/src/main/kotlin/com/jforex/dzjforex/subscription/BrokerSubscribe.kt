package com.jforex.dzjforex.subscription

import arrow.core.Try
import arrow.data.*
import arrow.instances.monad
import arrow.typeclasses.binding
import com.dukascopy.api.Instrument
import com.dukascopy.api.JFException
import com.dukascopy.api.instrument.IFinancialInstrument
import com.jforex.dzjforex.misc.*
import com.jforex.kforexutils.instrument.InstrumentFactory
import com.jforex.kforexutils.instrument.currencies
import io.reactivex.Observable
import org.apache.logging.log4j.LogManager

private val logger = LogManager.getLogger()

internal fun subscribeInstrument(assetName: String) = ReaderT { config: PluginConfig ->
    forexInstrumentFromAssetName(assetName).map {
        val instrumentsToSubscribe = getInstrumentsToSubscribe(it).runId(config)
        subscribeAllInstruments(instrumentsToSubscribe).runId(config)
        instrumentsToSubscribe
    }
}

internal fun subscribeAllInstruments(instrumentsToSubscribe: Set<Instrument>): Reader<PluginConfig, Unit> = ReaderApi
    .monad<PluginConfig>()
    .binding {
        val subscribedInstruments = subscribedInstruments().bind()
        val resultingInstruments = subscribedInstruments + instrumentsToSubscribe
        getContext {
            setSubscribedInstruments(resultingInstruments, true)
        }.bind()
    }.fix()

internal fun getInitialQuotes(instruments: Set<Instrument>) = ReaderT { config: PluginConfig ->
    Try {
        Observable
            .fromIterable(instruments)
            .map { instrument ->
                waitForFirstQuote(instrument)
                    .runId(config)
                    .fold({ throw JFException("No quote for $instrument available!") }, { it })
            }
            .toList()
            .blockingGet()
    }
}

internal fun isForexInstrument(instrument: Instrument) =
    if (instrument.type != IFinancialInstrument.Type.FOREX)
    {
        logger.debug("Currently only forex assets are supported and $instrument is not!")
        false
    } else true

internal fun subscribedInstruments() = getContext { subscribedInstruments }

private fun getInstrumentsToSubscribe(instrument: Instrument) = ReaderApi
    .monad<PluginConfig>()
    .binding {
        val instrumentWithCrosses = getInstrumentWithCrosses(instrument).bind()
        val subscribedInstruments = subscribedInstruments().bind()
        instrumentWithCrosses - subscribedInstruments
    }.fix()

private fun getInstrumentWithCrosses(instrument: Instrument) = ReaderApi
    .monad<PluginConfig>()
    .binding {
        val accountCurrency = getAccount { accountCurrency }.bind()
        val currencies = instrument.currencies + accountCurrency
        InstrumentFactory.fromCombinedCurrencies(currencies)
    }.fix()