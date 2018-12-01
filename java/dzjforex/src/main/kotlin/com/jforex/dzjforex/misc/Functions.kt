package com.jforex.dzjforex.misc

import arrow.core.None
import arrow.core.Some
import arrow.data.ReaderApi
import arrow.data.map
import arrow.data.runId
import com.dukascopy.api.system.ClientFactory
import com.dukascopy.api.system.IClient
import com.jforex.dzjforex.account.isTradingAllowedForAccount
import com.jforex.kforexutils.client.init
import com.jforex.kforexutils.instrument.InstrumentFactory
import io.reactivex.Single
import org.apache.logging.log4j.LogManager

private val logger = LogManager.getLogger()

internal fun getClient(): IClient {
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

internal fun isConnectedToDukascopy() =
    ReaderApi
        .ask<IClient>()
        .map { it.isConnected }

internal fun isConnected() =
    ReaderApi
        .ask<PluginEnvironment>()
        .map { it.client.isConnected }