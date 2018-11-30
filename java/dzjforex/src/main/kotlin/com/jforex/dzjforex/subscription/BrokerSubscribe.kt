package com.jforex.dzjforex.subscription

import arrow.data.ReaderApi
import arrow.data.fix
import arrow.data.map
import arrow.data.runId
import arrow.instances.monad
import arrow.typeclasses.binding
import com.dukascopy.api.Instrument
import com.dukascopy.api.instrument.IFinancialInstrument
import com.jforex.dzjforex.account.accountInfo
import com.jforex.dzjforex.misc.PluginEnvironment
import com.jforex.dzjforex.misc.instrumentFromAssetName
import com.jforex.dzjforex.zorro.ASSET_AVAILABLE
import com.jforex.dzjforex.zorro.ASSET_UNAVAILABLE
import com.jforex.dzjforex.zorro.SUBSCRIBE_FAIL
import com.jforex.dzjforex.zorro.SUBSCRIBE_OK
import com.jforex.kforexutils.instrument.InstrumentFactory
import com.jforex.kforexutils.instrument.currencies
import org.apache.logging.log4j.LogManager

private val logger = LogManager.getLogger()

internal fun subscribeAsset(assetName: String) = ReaderApi
    .ask<PluginEnvironment>()
    .map { env ->
        instrumentFromAssetName(assetName)
            .filter(::isForexInstrument)
            .map { getInstrumentsToSubscribe(it).runId(env) }
            .map {
                logger.debug("Subscribing instruments: $it")
                env.client.subscribedInstruments = it
            }
            .fold({ SUBSCRIBE_FAIL }) { SUBSCRIBE_OK }
    }

internal fun isForexInstrument(instrument: Instrument) =
    if (instrument.type != IFinancialInstrument.Type.FOREX) {
        logger.debug("Currently only forex assets are supported and $instrument is not!")
        false
    } else true

internal fun isInstrumentSubscribed(instrument: Instrument) = getSubscribedInstruments().map { it.contains(instrument) }

internal fun getSubscribedInstruments() = ReaderApi
    .ask<PluginEnvironment>()
    .map { env -> env.client.subscribedInstruments }

private fun getInstrumentsToSubscribe(instrument: Instrument) = ReaderApi
    .monad<PluginEnvironment>()
    .binding {
        val accountCurrency = accountInfo { accountCurrency }.bind()
        val currencies = instrument.currencies.plus(accountCurrency)
        InstrumentFactory.fromCombinedCurrencies(currencies)
    }.fix()