package com.jforex.dzjforex.misc

import com.dukascopy.api.system.ClientFactory
import com.dukascopy.api.system.IClient
import com.jforex.kforexutils.client.init
import com.jforex.kforexutils.misc.KForexUtils
import io.reactivex.Single
import org.apache.logging.log4j.LogManager

private val logger = LogManager.getLogger()

internal fun getClient(): IClient
{
    var client = Single
        .fromCallable { ClientFactory.getDefaultInstance() }
        .doOnError { logger.error("Error retrieving IClient instance! " + it.message) }
        .blockingGet()
    client.init()
    return client
}