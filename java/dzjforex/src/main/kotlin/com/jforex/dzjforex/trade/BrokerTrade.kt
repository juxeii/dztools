package com.jforex.dzjforex.trade

import arrow.Kind
import arrow.effects.ForIO
import arrow.effects.IO
import arrow.effects.instances.io.monadError.monadError
import arrow.typeclasses.MonadError
import arrow.typeclasses.binding
import arrow.typeclasses.bindingCatch
import com.dukascopy.api.Instrument
import com.dukascopy.api.JFException
import com.dukascopy.api.instrument.IFinancialInstrument
import com.jforex.dzjforex.misc.QuoteProviderDependencies
import com.jforex.dzjforex.misc.createQuoteProviderApi
import com.jforex.kforexutils.instrument.InstrumentFactory

fun createBrokerTradeApi(): BrokerTradeDependencies<ForIO> =
    BrokerTradeDependencies(createQuoteProviderApi(), IO.monadError())

interface BrokerTradeDependencies<F> : QuoteProviderDependencies, MonadError<F, Throwable>
{
    companion object
    {
        operator fun <F> invoke(
            quoteProviderDependencies: QuoteProviderDependencies,
            ME: MonadError<F, Throwable>
        ): BrokerTradeDependencies<F> =
            object : BrokerTradeDependencies<F>,
                QuoteProviderDependencies by quoteProviderDependencies,
                MonadError<F, Throwable> by ME
            {}
    }
}

object BrokerTradeApi
{
    fun <F> BrokerTradeDependencies<F>.create(
        orderId: Int,
        out_TradeInfoToFill: DoubleArray
    ): Kind<F, Int> = binding {
        //1.find order
        //2.fill params

        42
    }
}
