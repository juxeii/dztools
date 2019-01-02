package com.jforex.dzjforex.init

import arrow.Kind
import com.jforex.dzjforex.misc.PluginDependencies
import com.jforex.dzjforex.misc.getStackTrace
import com.jforex.dzjforex.misc.initContextApi
import com.jforex.dzjforex.misc.logger
import com.jforex.dzjforex.order.initOrderRepositoryApi
import com.jforex.dzjforex.zorro.BROKER_INIT_FAIL
import com.jforex.dzjforex.zorro.BROKER_INIT_OK
import com.jforex.kforexutils.misc.kForexUtils
import com.jforex.kforexutils.strategy.KForexUtilsStrategy

object BrokerInitApi
{
    fun <F> PluginDependencies<F>.brokerInit(): Kind<F, Int> =
        startStrategy()
            .flatMap { initApis() }
            .map { BROKER_INIT_OK }
            .handleError { error ->
                logger.error("BrokerInit failed! Error: $error Stack trace: ${getStackTrace(error)}")
                BROKER_INIT_FAIL
            }

    fun <F> PluginDependencies<F>.startStrategy(): Kind<F, Long> = invoke {
        val strategy = KForexUtilsStrategy()
        client.startStrategy(strategy)
    }

    fun <F> PluginDependencies<F>.initApis(): Kind<F, Unit> = invoke {
        initContextApi(kForexUtils.context)
        initOrderRepositoryApi()
    }
}