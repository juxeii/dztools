package com.jforex.dzjforex.buy

import arrow.Kind
import arrow.effects.ForIO
import arrow.effects.IO
import arrow.effects.instances.io.monadError.monadError
import arrow.typeclasses.MonadError
import arrow.typeclasses.bindingCatch
import com.dukascopy.api.IEngine
import com.dukascopy.api.IOrder
import com.jforex.dzjforex.misc.*
import com.jforex.dzjforex.misc.InstrumentApi.fromAssetName
import com.jforex.dzjforex.misc.PluginApi.contractsToAmount
import com.jforex.dzjforex.order.storeOrder
import com.jforex.dzjforex.zorro.BROKER_BUY_FAIL
import com.jforex.kforexutils.engine.submit
import com.jforex.kforexutils.order.event.OrderEventType
import java.util.concurrent.TimeUnit

lateinit var brokerBuyApi: BrokerBuyDependencies<ForIO>

fun initBrokerBuyApi()
{
    brokerBuyApi = BrokerBuyDependencies(pluginApi, contextApi, IO.monadError())
}

interface BrokerBuyDependencies<F> : PluginDependencies,
    ContextDependencies,
    InstrumentFunc<F>,
    MonadError<F, Throwable>
{
    companion object
    {
        operator fun <F> invoke(
            pluginDependencies: PluginDependencies,
            contextDependencies: ContextDependencies,
            ME: MonadError<F, Throwable>
        ): BrokerBuyDependencies<F> =
            object : BrokerBuyDependencies<F>,
                PluginDependencies by pluginDependencies,
                ContextDependencies by contextDependencies,
                MonadError<F, Throwable> by ME
            {}
    }
}

object BrokerBuyApi
{
    fun <F> BrokerBuyDependencies<F>.create(
        assetName: String,
        contracts: Int,
        slDistance: Double,
        limit: Double,
        out_TradeInfoToFill: DoubleArray
    ): Kind<F, Int> =
        bindingCatch {
            val instrument = fromAssetName(assetName).bind()
            logger.debug("Starting to submit!")
            val order: IOrder = engine
                .submit(
                    label = "ZorroTest",
                    instrument = instrument,
                    orderCommand = IEngine.OrderCommand.BUY,
                    amount = contractsToAmount(contracts)
                ) {
                    onStart = { logger.debug("Starting to submit!") }
                }
                .timeout(30, TimeUnit.SECONDS)
                .takeUntil { it.type == OrderEventType.FULLY_FILLED }
                .map { it.order }
                .blockingFirst()

            storeOrder(order)

            out_TradeInfoToFill[0] = order.openPrice

            order.id.toInt()
        }.handleError {
            logger.debug("Zefix!! $it")
            BROKER_BUY_FAIL
        }
}
