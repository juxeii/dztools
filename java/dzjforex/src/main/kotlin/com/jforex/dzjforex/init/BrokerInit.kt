package com.jforex.dzjforex.init

import com.jforex.dzjforex.misc.*
import com.jforex.kforexutils.misc.kForexUtils
import com.jforex.kforexutils.strategy.KForexUtilsStrategy
import io.reactivex.Observable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

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
        logger.debug("Starting info strategy...")
        val strategy = KForexUtilsStrategy()
        client.startStrategy(strategy)
        logger.debug("Info strategy successfully started.")
    }

    fun <F> PluginDependencies<F>.startTickTriggerRoutine() = delay {

        if (pluginSettings.useTickCallback())
        {
            logger.debug("Starting tick trigger daemon for tick callback...")
            kForexUtils
                .tickQuotes
                .distinctUntilChanged { quoteA, quoteB -> quoteA.tick.ask == quoteB.tick.ask }
                .observeOn(Schedulers.io())
                .subscribeBy(onNext = { natives.triggerQuoteReq() })
        }

        Observable
            .interval(1L, 1L, TimeUnit.MINUTES)
            .observeOn(Schedulers.io())
            .subscribeBy(onNext = { printHeapInfo() })
    }
}