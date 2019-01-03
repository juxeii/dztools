package com.jforex.dzjforex.init

import com.jforex.dzjforex.misc.PluginDependencies
import com.jforex.dzjforex.misc.getStackTrace
import com.jforex.dzjforex.misc.initContextApi
import com.jforex.dzjforex.misc.logger
import com.jforex.dzjforex.order.initOrderRepositoryApi
import com.jforex.kforexutils.misc.kForexUtils
import com.jforex.kforexutils.strategy.KForexUtilsStrategy

object BrokerInitApi
{
    fun <F> PluginDependencies<F>.brokerInit() =
        startStrategy()
            .flatMap { initApis() }
            .handleError { error ->
                logger.error(
                    "BrokerInit failed! Error message: ${error.message}" +
                            " Stack trace: ${getStackTrace(error)}"
                )
            }

    fun <F> PluginDependencies<F>.startStrategy() = invoke {
        val strategy = KForexUtilsStrategy()
        client.startStrategy(strategy)
    }

    fun <F> PluginDependencies<F>.initApis() = invoke {
        initContextApi(kForexUtils.context)
        initOrderRepositoryApi()
    }
}