package com.jforex.dzjforex.misc

import com.dukascopy.api.IAccount
import com.dukascopy.api.IContext
import com.dukascopy.api.IHistory
import com.dukascopy.api.system.IClient
import com.jforex.dzjforex.settings.PluginSettings
import com.jforex.kforexutils.misc.KForexUtils
import com.jforex.kforexutils.strategy.KForexUtilsStrategy
import org.apache.logging.log4j.LogManager

class PluginStrategy(
    private val client: IClient,
    private val pluginSettings: PluginSettings
)
{
    private val infoStrategy = KForexUtilsStrategy()
    private lateinit var kForexUtils: KForexUtils
    lateinit var context: IContext
        private set
    lateinit var quoteProvider: QuoteProvider
        private set
    lateinit var account: IAccount
        private set
    lateinit var history: IHistory
        private set

    private val logger = LogManager.getLogger(PluginStrategy::class.java)

    fun start()
    {
        client.startStrategy(infoStrategy)
        logger.debug("started strategy")
        kForexUtils = infoStrategy.kForexUtilsSingle().blockingFirst()
        context = kForexUtils.context
        account = context.account
        history = context.history
        quoteProvider = QuoteProvider(kForexUtils)
    }
}