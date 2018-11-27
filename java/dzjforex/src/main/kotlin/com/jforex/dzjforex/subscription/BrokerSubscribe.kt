package com.jforex.dzjforex.subscription

import com.dukascopy.api.Instrument
import com.dukascopy.api.system.IClient
import com.jforex.dzjforex.account.AccountInfo
import com.jforex.dzjforex.misc.instrumentFromAssetName
import com.jforex.dzjforex.zorro.ASSET_AVAILABLE
import com.jforex.dzjforex.zorro.ASSET_UNAVAILABLE
import com.jforex.kforexutils.instrument.InstrumentFactory
import com.jforex.kforexutils.instrument.currencies
import org.apache.logging.log4j.LogManager

private val logger = LogManager.getLogger()

class BrokerSubscribe(
    private val client: IClient,
    private val accountInfo: AccountInfo
) {

    fun subscribe(assetName: String) = instrumentFromAssetName(assetName)
        .filter { !isSubscribed(it) }
        .map(::instrumentsToSubscribe)
        .map {
            logger.debug("Subscribing instruments: $it")
            client.subscribedInstruments = it
        }
        .fold({ ASSET_UNAVAILABLE }) { ASSET_AVAILABLE }

    private fun instrumentsToSubscribe(instrument: Instrument) = InstrumentFactory.fromCombinedCurrencies(
        instrument.currencies.plus(accountInfo.accountCurrency)
    )

    fun subscribedInstruments() = client.subscribedInstruments

    fun isSubscribed(instrument: Instrument) = subscribedInstruments().contains(instrument)
}