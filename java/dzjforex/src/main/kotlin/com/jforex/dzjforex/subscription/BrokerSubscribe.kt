package com.jforex.dzjforex.subscription

import arrow.data.*
import arrow.instances.monad
import arrow.typeclasses.binding
import com.dukascopy.api.Instrument
import com.dukascopy.api.JFException
import com.dukascopy.api.instrument.IFinancialInstrument
import com.jforex.dzjforex.misc.*
import com.jforex.dzjforex.zorro.SUBSCRIBE_FAIL
import com.jforex.dzjforex.zorro.SUBSCRIBE_OK
import com.jforex.dzjforex.zorro.progressWait
import com.jforex.kforexutils.instrument.InstrumentFactory
import com.jforex.kforexutils.instrument.currencies
import io.reactivex.Observable
import org.apache.logging.log4j.LogManager

private val logger = LogManager.getLogger()

internal fun subscribeAsset(assetName: String): Reader<PluginEnvironment, Int> =
    instrumentFromAssetName(assetName)
        .filter(::isForexInstrument)
        .fold({ Reader().just(SUBSCRIBE_FAIL) }) { subscribeValidInstrument(it) }

internal fun subscribeValidInstrument(instrument: Instrument) = getInstrumentsToSubscribe(instrument)
    .flatMap { setSubscribedInstruments(it) }
    .flatMap { waitForQuotes(it) }
    .flatMap { progressWait(it) }

internal fun waitForQuotes(instruments: Set<Instrument>) = ReaderApi
    .ask<PluginEnvironment>()
    .map { env ->
        Observable
            .fromIterable(instruments)
            .map { instrument ->
                waitForFirstQuote(instrument)
                    .runId(env)
                    .fold({ throw JFException("No quote for $instrument available!") }, { instrument })
            }
            .ignoreElements()
            .toSingleDefault(SUBSCRIBE_OK)
            .onErrorReturnItem(SUBSCRIBE_FAIL)
    }

internal fun isForexInstrument(instrument: Instrument) =
    if (instrument.type != IFinancialInstrument.Type.FOREX)
    {
        logger.debug("Currently only forex assets are supported and $instrument is not!")
        false
    } else true

internal fun isInstrumentSubscribed(instrument: Instrument) = getSubscribedInstruments().map { it.contains(instrument) }

internal fun getSubscribedInstruments() = ReaderApi
    .ask<PluginEnvironment>()
    .map { env ->
        env
            .pluginStrategy
            .context
            .subscribedInstruments
    }

internal fun setSubscribedInstruments(instruments: Set<Instrument>) = ReaderApi
    .ask<PluginEnvironment>()
    .map { env ->
        logger.debug("Subscribing instruments: $instruments")
        env
            .pluginStrategy
            .context
            .setSubscribedInstruments(instruments, true)
        instruments
    }

private fun getInstrumentsToSubscribe(instrument: Instrument) = ReaderApi
    .monad<PluginEnvironment>()
    .binding {
        val accountCurrency = getAccount { accountCurrency }.bind()
        val currencies = instrument.currencies.plus(accountCurrency)
        InstrumentFactory.fromCombinedCurrencies(currencies) - quotesInstruments().bind()
    }.fix()