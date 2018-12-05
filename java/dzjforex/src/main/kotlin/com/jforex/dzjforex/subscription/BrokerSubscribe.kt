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
import com.jforex.kforexutils.instrument.InstrumentFactory
import com.jforex.kforexutils.instrument.currencies
import io.reactivex.Observable
import io.reactivex.Single
import org.apache.logging.log4j.LogManager

private val logger = LogManager.getLogger()

internal fun getSubscribeTask(assetName: String): Reader<PluginConfig, Single<Int>> =
    instrumentFromAssetName(assetName)
        .filter(::isForexInstrument)
        .fold({ Reader().just(Single.just(SUBSCRIBE_FAIL)) }) { subscribeValidInstrument(it) }

internal fun subscribeValidInstrument(instrument: Instrument) = getInstrumentsToSubscribe(instrument)
    .flatMap { setSubscribedInstruments(it) }
    .flatMap { waitForQuotes(it) }

internal fun waitForQuotes(instruments: Set<Instrument>) = ReaderApi
    .ask<PluginConfig>()
    .map { config ->
        Observable
            .fromIterable(instruments)
            .map { instrument ->
                waitForFirstQuote(instrument)
                    .runId(config)
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
    .ask<PluginConfig>()
    .map { config ->
        config
            .kForexUtils
            .context
            .subscribedInstruments
    }

internal fun setSubscribedInstruments(instruments: Set<Instrument>) = ReaderApi
    .ask<PluginConfig>()
    .map { config ->
        logger.debug("Subscribing instruments: $instruments")
        config
            .kForexUtils
            .context
            .setSubscribedInstruments(instruments, true)
        instruments
    }

private fun getInstrumentsToSubscribe(instrument: Instrument) = ReaderApi
    .monad<PluginConfig>()
    .binding {
        val accountCurrency = getAccount { accountCurrency }.bind()
        val currencies = instrument.currencies.plus(accountCurrency)
        InstrumentFactory.fromCombinedCurrencies(currencies) - quotesInstruments().bind()
    }.fix()