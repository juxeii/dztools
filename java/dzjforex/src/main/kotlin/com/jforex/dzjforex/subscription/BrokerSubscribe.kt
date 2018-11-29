package com.jforex.dzjforex.subscription

import arrow.data.ReaderApi
import arrow.data.map
import arrow.data.runId
import com.dukascopy.api.Instrument
import com.jforex.dzjforex.account.accountCurrency
import com.jforex.dzjforex.misc.PluginEnvironment
import com.jforex.dzjforex.misc.instrumentFromAssetName
import com.jforex.dzjforex.zorro.ASSET_AVAILABLE
import com.jforex.dzjforex.zorro.ASSET_UNAVAILABLE
import com.jforex.kforexutils.instrument.InstrumentFactory
import com.jforex.kforexutils.instrument.currencies
import org.apache.logging.log4j.LogManager

private val logger = LogManager.getLogger()

internal fun subscribeAsset(assetName: String) = ReaderApi
    .ask<PluginEnvironment>()
    .map { env ->
        instrumentFromAssetName(assetName)
            .map { getInstrumentsToSubscribe(it).runId(env) }
            .map {
                logger.debug("Subscribing instruments: $it")
                env.client.subscribedInstruments = it
            }
            .fold({ ASSET_UNAVAILABLE }) { ASSET_AVAILABLE }
    }

internal fun isInstrumentSubscribed(instrument: Instrument) = getSubscribedInstruments().map { it.contains(instrument) }

internal fun getSubscribedInstruments() = ReaderApi
    .ask<PluginEnvironment>()
    .map { env -> env.client.subscribedInstruments }

private fun getInstrumentsToSubscribe(instrument: Instrument) = ReaderApi
    .ask<PluginEnvironment>()
    .map { env ->
        val accountCurrency = accountCurrency().runId(env)
        val currencies = instrument.currencies.plus(accountCurrency)
        InstrumentFactory.fromCombinedCurrencies(currencies)
    }