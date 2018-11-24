package com.jforex.dzjforex.misc

import com.dukascopy.api.IContext
import com.dukascopy.api.Instrument
import com.dukascopy.api.system.IClient
import com.jforex.dzjforex.account.AccountInfo
import com.jforex.dzjforex.settings.PluginSettings
import com.jforex.kforexutils.misc.KForexUtils
import com.jforex.kforexutils.strategy.KForexUtilsStrategy

class PluginStrategy(
    private val client: IClient,
    private val pluginSettings: PluginSettings
)
{
    val infoStrategy = KForexUtilsStrategy()
    private var strategyID = 0L
    private lateinit var kForexUtils: KForexUtils
    private lateinit var context: IContext
    lateinit var accountInfo: AccountInfo
        private set

    fun start(accountInfoToFill: Array<String>)
    {
        client.subscribedInstruments = setOf(Instrument.EURUSD)
        strategyID = client.startStrategy(infoStrategy);
        kForexUtils = infoStrategy.kForexUtils
        context = kForexUtils.context
        accountInfo = AccountInfo(context.account, pluginSettings)
        accountInfoToFill[0] = accountInfo.accountId
    }

    fun stop()
    {
        client.stopStrategy(strategyID)
    }
}