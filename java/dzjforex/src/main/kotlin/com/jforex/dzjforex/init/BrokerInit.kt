package com.jforex.dzjforex.init

import com.jforex.dzjforex.misc.PluginDependencies
import com.jforex.dzjforex.misc.getStackTrace
import com.jforex.dzjforex.misc.initContextApi
import com.jforex.dzjforex.misc.logger
import com.jforex.kforexutils.misc.kForexUtils
import com.jforex.kforexutils.strategy.KForexUtilsStrategy

object BrokerInitApi
{
    fun <F> PluginDependencies<F>.brokerInit() =
        startStrategy()
            .map { initContextApi(kForexUtils.context) }
            .handleError { error ->
                logger.error(
                    "BrokerInit failed! Error message: ${error.message}" +
                            " Stack trace: ${getStackTrace(error)}"
                )
            }

    fun <F> PluginDependencies<F>.startStrategy() = delay {
        val strategy = KForexUtilsStrategy()
        client.startStrategy(strategy)
    }
}