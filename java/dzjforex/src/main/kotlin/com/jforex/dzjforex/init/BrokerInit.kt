package com.jforex.dzjforex.init

import com.jforex.dzjforex.misc.PluginDependencies
import com.jforex.dzjforex.misc.getStackTrace
import com.jforex.dzjforex.misc.initContextApi
import com.jforex.dzjforex.misc.logger
import com.jforex.kforexutils.misc.kForexUtils
import com.jforex.kforexutils.strategy.KForexUtilsStrategy
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

object BrokerInitApi
{
    fun <F> PluginDependencies<F>.brokerInit() =
        startStrategy()
            .map { initContextApi(kForexUtils.context) }
            .flatMap { startTickTriggerRoutine() }
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

    fun <F> PluginDependencies<F>.startTickTriggerRoutine() = delay {
        logger.debug("Starting tick trigger daemon...")
        kForexUtils
            .tickQuotes
            .distinctUntilChanged { quoteA, quoteB -> quoteA.tick.ask == quoteB.tick.ask }
            .takeUntil { !client.isConnected }
            .observeOn(Schedulers.io())
            .subscribeBy(onNext = { natives.triggerQuoteReq()})
    }
}