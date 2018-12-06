package com.jforex.dzjforex.misc

import arrow.core.Failure
import arrow.core.success
import arrow.data.ReaderApi
import arrow.data.map
import com.dukascopy.api.IAccount
import com.dukascopy.api.IContext
import com.dukascopy.api.IHistory
import com.dukascopy.api.JFException
import com.dukascopy.api.system.ClientFactory
import com.dukascopy.api.system.IClient
import com.jforex.dzjforex.subscription.isForexInstrument
import com.jforex.kforexutils.client.init
import com.jforex.kforexutils.instrument.InstrumentFactory
import io.reactivex.Single
import org.apache.logging.log4j.LogManager

private val logger = LogManager.getLogger()

internal fun getClient(): IClient
{
    var client = Single
        .fromCallable { ClientFactory.getDefaultInstance() }
        .doOnError { logger.debug("Error retrieving IClient instance! " + it.message) }
        .blockingGet()
    client.init()
    return client
}

internal fun instrumentFromAssetName(assetName: String) = InstrumentFactory
    .fromName(assetName)
    .fold({
        Failure(JFException("Cannot create instrument from asset name $assetName!"))
    }, { it.success() })

internal fun forexInstrumentFromAssetName(assetName: String) = instrumentFromAssetName(assetName)
    .filter(::isForexInstrument)
    .fold({
        Failure(JFException("Asset $assetName is not a Forex instrument!"))
    }, { it.success() })

internal fun <R> getClient(block: IClient.() -> R) = ReaderApi
    .ask<PluginConfig>()
    .map { config ->
        config
            .client
            .run(block)
    }

internal fun <R> getContext(block: IContext.() -> R) = ReaderApi
    .ask<PluginConfig>()
    .map { config ->
        config
            .kForexUtils
            .context
            .run(block)
    }

internal fun <R> getAccount(block: IAccount.() -> R) = ReaderApi
    .ask<PluginConfig>()
    .map { config ->
        config
            .kForexUtils
            .context
            .account
            .run(block)
    }

internal fun <R> getHistory(block: IHistory.() -> R) = ReaderApi
    .ask<PluginConfig>()
    .map { config ->
        config
            .kForexUtils
            .context
            .history
            .run(block)
    }