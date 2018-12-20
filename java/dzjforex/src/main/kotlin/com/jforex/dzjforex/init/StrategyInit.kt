package com.jforex.dzjforex.init

import arrow.Kind
import arrow.typeclasses.ApplicativeError
import com.dukascopy.api.IAccount
import com.dukascopy.api.IContext
import com.dukascopy.api.system.IClient
import com.jforex.dzjforex.misc.Quotes
import com.jforex.dzjforex.zorro.saveQuote
import com.jforex.kforexutils.misc.KForexUtils
import com.jforex.kforexutils.strategy.KForexUtilsStrategy
import io.reactivex.rxkotlin.subscribeBy

var quotes: Quotes = emptyMap()

lateinit var kForexUtils: KForexUtils
lateinit var context: IContext
lateinit var account: IAccount

interface StrategyInitDependencies<F> : ApplicativeError<F, Throwable>
{
    val client: IClient
    val strategy: KForexUtilsStrategy

    companion object
    {
        operator fun <F> invoke(
            AE: ApplicativeError<F, Throwable>,
            client: IClient,
            strategy: KForexUtilsStrategy
        ): StrategyInitDependencies<F> =
            object : StrategyInitDependencies<F>, ApplicativeError<F, Throwable> by AE
            {
                override val client = client
                override val strategy = strategy
            }
    }
}

object StrategyInitApi
{
    fun <F> StrategyInitDependencies<F>.start(): Kind<F, Unit> = catch {
        client.startStrategy(strategy)
        kForexUtils = strategy.kForexUtilsSingle().blockingFirst()
        kForexUtils
            .tickQuotes
            .subscribeBy(onNext = { saveQuote(it) })
        context = kForexUtils.context
        account = context.account
    }
}