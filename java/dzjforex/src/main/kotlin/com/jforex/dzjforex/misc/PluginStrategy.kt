package com.jforex.dzjforex.misc

import com.dukascopy.api.Instrument
import com.dukascopy.api.system.IClient
import com.jforex.kforexutils.strategy.KForexUtilsStrategy

class PluginStrategy(private val client: IClient)
{
    val infoStrategy = KForexUtilsStrategy()
    private var strategyID = 0L

    fun start(accountInfoToFill: Array<String>)
    {
        client.subscribedInstruments = setOf(Instrument.EURUSD)
        strategyID = client.startStrategy(infoStrategy);
    }

    fun stop()
    {
        client.stopStrategy(strategyID)
    }
}