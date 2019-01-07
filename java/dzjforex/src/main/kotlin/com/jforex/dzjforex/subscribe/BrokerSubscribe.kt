package com.jforex.dzjforex.subscribe

import com.dukascopy.api.Instrument
import com.jforex.dzjforex.misc.ContextApi.setSubscribedInstruments
import com.jforex.dzjforex.misc.ContextDependencies
import com.jforex.dzjforex.misc.PluginApi.createInstrument
import com.jforex.dzjforex.misc.PluginApi.filterTradeableInstrument
import com.jforex.dzjforex.misc.getStackTrace
import com.jforex.dzjforex.misc.logger
import com.jforex.dzjforex.zorro.SUBSCRIBE_FAIL
import com.jforex.dzjforex.zorro.SUBSCRIBE_OK
import com.jforex.kforexutils.instrument.InstrumentFactory
import com.jforex.kforexutils.instrument.currencies

object BrokerSubscribeApi
{
    fun <F> ContextDependencies<F>.brokerSubscribe(assetName: String) =
        createInstrument(assetName)
            .flatMap { instrument ->
                logger.debug("Subscribing asset $assetName")
                getInstrumentWithCrosses(instrument)
            }
            .flatMap { instrumentsToSubscribe -> setSubscribedInstruments(instrumentsToSubscribe) }
            .map {
                logger.debug(
                    "Successfully subscribed asset $assetName." +
                            " All subscribed assets: ${jfContext.subscribedInstruments}"
                )
                SUBSCRIBE_OK
            }
            .handleError { error ->
                logger.debug(
                    "BrokerSubscribe failed! " +
                            "Error message: ${error.message} " +
                            "Stack trace: ${getStackTrace(error)}"
                )
                SUBSCRIBE_FAIL
            }

    fun <F> ContextDependencies<F>.getInstrumentWithCrosses(instrument: Instrument) = delay {
        InstrumentFactory.fromCombinedCurrencies(instrument.currencies + account.accountCurrency)
    }
}
