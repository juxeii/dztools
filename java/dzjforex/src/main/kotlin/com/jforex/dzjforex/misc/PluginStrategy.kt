package com.jforex.dzjforex.misc

import com.dukascopy.api.IAccount
import com.dukascopy.api.IContext
import com.dukascopy.api.Instrument
import com.dukascopy.api.system.IClient
import com.jforex.dzjforex.settings.PluginSettings
import com.jforex.dzjforex.zorro.KZorroBridge
import com.jforex.kforexutils.misc.KForexUtils
import com.jforex.kforexutils.strategy.KForexUtilsStrategy
import org.apache.logging.log4j.LogManager

class PluginStrategy(
    private val client: IClient,
    private val pluginSettings: PluginSettings
) {
    private val infoStrategy = KForexUtilsStrategy()
    private var strategyID = 0L
    private lateinit var kForexUtils: KForexUtils
    lateinit var context: IContext
        private set
    lateinit var quoteProvider: QuoteProvider
        private set
    lateinit var account: IAccount
        private set

    private val logger = LogManager.getLogger(PluginStrategy::class.java)

    fun start(out_AccountNames: Array<String>) {
        client.subscribedInstruments = setOf(Instrument.EURUSD)
        logger.debug("Subscribed instruments")
        strategyID = client.startStrategy(infoStrategy)
        logger.debug("started strategy")
        kForexUtils = infoStrategy.kForexUtils
        context = kForexUtils.context
        account = context.account
        quoteProvider = QuoteProvider(kForexUtils)
        out_AccountNames[0] = account.accountId
        logger.debug("filled account params")
    }

    fun stop() {
        client.stopStrategy(strategyID)
    }
}