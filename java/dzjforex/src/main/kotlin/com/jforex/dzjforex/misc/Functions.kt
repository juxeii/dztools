package com.jforex.dzjforex.misc

import arrow.core.None
import arrow.core.Some
import arrow.data.ReaderApi
import arrow.data.map
import com.dukascopy.api.IAccount
import com.dukascopy.api.IContext
import com.dukascopy.api.IHistory
import com.dukascopy.api.system.ClientFactory
import com.dukascopy.api.system.IClient
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
        logger.debug("Cannot create instrument from asset name $assetName!")
        None
    }, { Some(it) })

internal fun <R> getClient(block: IClient.() -> R) = ReaderApi
    .ask<PluginEnvironment>()
    .map { env ->
        env
            .client
            .run(block)
    }

internal fun <R> getContext(block: IContext.() -> R) = ReaderApi
    .ask<PluginEnvironment>()
    .map { env ->
        env
            .pluginStrategy
            .context
            .run(block)
    }

internal fun <R> getAccount(block: IAccount.() -> R) = ReaderApi.ask<PluginEnvironment>()
    .map { env ->
        env
            .pluginStrategy
            .account
            .run(block)
    }

internal fun <R> getHistory(block: IHistory.() -> R) = ReaderApi
    .ask<PluginEnvironment>()
    .map { env ->
        env
            .pluginStrategy
            .history
            .run(block)
    }