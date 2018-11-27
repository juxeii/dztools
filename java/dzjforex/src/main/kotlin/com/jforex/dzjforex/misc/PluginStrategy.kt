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
) {
    private val infoStrategy = KForexUtilsStrategy()
    private var strategyID = 0L
    private lateinit var kForexUtils: KForexUtils
    lateinit var context: IContext
        private set
    lateinit var accountInfo: AccountInfo
        private set
    lateinit var quoteProvider: QuoteProvider
        private set


    fun start(out_AccountNames: Array<String>) {
        client.subscribedInstruments = setOf(Instrument.EURUSD)
        strategyID = client.startStrategy(infoStrategy);
        kForexUtils = infoStrategy.kForexUtils
        context = kForexUtils.context
        accountInfo = AccountInfo(context.account, pluginSettings)
        quoteProvider = QuoteProvider(kForexUtils)
        out_AccountNames[0] = accountInfo.accountId
    }

    fun stop() {
        client.stopStrategy(strategyID)
    }
}