package com.jforex.dzjforex.init

import arrow.Kind
import com.jforex.dzjforex.misc.*
import com.jforex.dzjforex.order.initOrderRepositoryApi
import com.jforex.dzjforex.zorro.BROKER_INIT_FAIL
import com.jforex.dzjforex.zorro.BROKER_INIT_OK
import com.jforex.kforexutils.misc.kForexUtils
import com.jforex.kforexutils.strategy.KForexUtilsStrategy
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy

object BrokerInitApi
{
    fun <F> PluginDependencies<F>.brokerInit(): Kind<F, Int> =
        startStrategy()
            .flatMap { subscribeToTicks() }
            .flatMap { initApis() }
            .map { BROKER_INIT_OK }
            .handleError { initError ->
                logger.error("Initialization failed! Error: $initError Stack trace: ${getStackTrace(initError)}")
                BROKER_INIT_FAIL
            }

    fun <F> PluginDependencies<F>.startStrategy(): Kind<F, Long> = invoke {
        val strategy = KForexUtilsStrategy()
        client.startStrategy(strategy)
    }

    fun <F> PluginDependencies<F>.subscribeToTicks(): Kind<F, Disposable> = invoke {
        kForexUtils
            .tickQuotes
            .subscribeBy(onNext = { saveQuote(it) })
    }

    fun <F> PluginDependencies<F>.initApis(): Kind<F, Unit> = invoke {
        initContextApi(kForexUtils.context)
        initOrderRepositoryApi()
    }
}