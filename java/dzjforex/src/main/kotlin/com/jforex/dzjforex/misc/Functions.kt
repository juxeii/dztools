package com.jforex.dzjforex.misc

import arrow.Kind
import arrow.core.Failure
import arrow.core.success
import arrow.typeclasses.ApplicativeError
import com.dukascopy.api.Instrument
import com.dukascopy.api.JFException
import com.dukascopy.api.instrument.IFinancialInstrument
import com.dukascopy.api.system.ClientFactory
import com.dukascopy.api.system.IClient
import com.jforex.kforexutils.client.init
import com.jforex.kforexutils.instrument.InstrumentFactory
import io.reactivex.Single
import org.apache.logging.log4j.LogManager

private val logger = LogManager.getLogger()

fun getClient(): IClient
{
    var client = Single
        .fromCallable { ClientFactory.getDefaultInstance() }
        .doOnError { logger.debug("Error retrieving IClient instance! " + it.message) }
        .blockingGet()
    client.init()
    return client
}

fun instrumentFromAssetName(assetName: String) = InstrumentFactory
    .fromName(assetName)
    .fold({
        Failure(JFException("Cannot create instrument from asset name $assetName!"))
    }, { it.success() })

fun forexInstrumentFromAssetName(assetName: String) = instrumentFromAssetName(assetName)
    .filter(::isForexInstrument)
    .fold({
        Failure(JFException("Asset $assetName is not a Forex instrument!"))
    }, { it.success() })

fun isForexInstrument(instrument: Instrument) =
    if (instrument.type != IFinancialInstrument.Type.FOREX)
    {
        logger.debug("Currently only forex assets are supported and $instrument is not!")
        false
    } else true
