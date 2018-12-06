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
import com.jforex.kforexutils.price.TickQuote
import io.reactivex.Observable
import org.apache.logging.log4j.LogManager

private val logger = LogManager.getLogger()

internal fun subscribeInstrument(assetName: String): Reader<PluginConfig, Try<Set<Instrument>>> = ReaderApi
    .monad<PluginConfig>()
    .binding {
        getInstrumentsToSubscribe(assetName)
            .bind()
            .map {
                subscribeInstruments(it).bind()
                it
            }
    }.fix()

internal fun getInstrumentsToSubscribe(assetName: String): Reader<PluginConfig, Try<Set<Instrument>>> = ReaderApi
    .ask<PluginConfig>()
    .map { config -> forexInstrumentFromAssetName(assetName).map { getInstrumentsToSubscribe(it).runId(config) } }

internal fun subscribeInstruments(instrumentsToSubscribe: Set<Instrument>): Reader<PluginConfig, Unit> = ReaderApi
    .monad<PluginConfig>()
    .binding {
        val subscribedInstruments = getSubscribedInstruments().bind()
        val resultingInstruments = subscribedInstruments + instrumentsToSubscribe
        getContext { setSubscribedInstruments(resultingInstruments, true) }.bind()
    }.fix()

internal fun getInitialQuotes(instruments: Set<Instrument>): Reader<PluginConfig, Try<List<TickQuote>>> = ReaderApi
    .ask<PluginConfig>()
    .map { config ->
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

internal fun getSubscribedInstruments() = getContext { subscribedInstruments }

private fun getInstrumentsToSubscribe(instrument: Instrument) = ReaderApi
    .monad<PluginConfig>()
    .binding {
        val instrumentWithCrosses = getInstrumentWithCrosses(instrument).bind()
        val subscribedInstruments = getSubscribedInstruments().bind()
        instrumentWithCrosses - subscribedInstruments
    }.fix()

private fun getInstrumentWithCrosses(instrument: Instrument) = ReaderApi
    .monad<PluginConfig>()
    .binding {
        val accountCurrency = getAccount { accountCurrency }.bind()
        val currencies = instrument.currencies + accountCurrency
        InstrumentFactory.fromCombinedCurrencies(currencies)
    }.fix()