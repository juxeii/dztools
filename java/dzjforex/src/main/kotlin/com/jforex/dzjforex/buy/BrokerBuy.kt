package com.jforex.dzjforex.buy

import arrow.Kind
import arrow.core.Option
import arrow.typeclasses.ApplicativeError
import arrow.typeclasses.binding
import com.dukascopy.api.IEngine
import com.dukascopy.api.IOrder
import com.dukascopy.api.Instrument
import com.dukascopy.api.JFException
import com.jforex.dzjforex.misc.*
import com.jforex.dzjforex.misc.InstrumentApi.filterTradeable
import com.jforex.dzjforex.misc.InstrumentApi.fromAssetName
import com.jforex.dzjforex.quote.QuotesProviderApi.getAsk
import com.jforex.dzjforex.quote.QuotesProviderApi.getBid
import com.jforex.dzjforex.order.zorroId
import com.jforex.dzjforex.quote.createQuoteProviderApi
import com.jforex.dzjforex.zorro.BROKER_BUY_FAIL
import com.jforex.dzjforex.zorro.BROKER_BUY_OPPOSITE_CLOSE
import com.jforex.dzjforex.zorro.BROKER_BUY_TIMEOUT
import com.jforex.kforexutils.engine.submit
import com.jforex.kforexutils.misc.asPrice
import com.jforex.kforexutils.order.event.OrderEventType
import com.jforex.kforexutils.price.Price
import com.jforex.kforexutils.settings.TradingSettings
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

sealed class BrokerBuyResult(val returnCode: Int)
{
    data class Failure(val code: Int) : BrokerBuyResult(code)
    data class Success(val code: Int, val price: Double) : BrokerBuyResult(code)
}
typealias BrokerBuyFailure = BrokerBuyResult.Failure
typealias BrokerBuySuccess = BrokerBuyResult.Success

fun createBrokerBuyApi() = QuoteDependencies(contextApi, createQuoteProviderApi())

class LocalDataSource<F>(A: ApplicativeError<F, Throwable>) : ApplicativeError<F, Throwable> by A
{

    private val localCache: Map<Int, List<Double>> =
        mapOf(1 to listOf(3.0))

    fun allTasksByUser(user: Int): Kind<F, List<Double>> =
        Option.fromNullable(localCache[user]).fold(
            { raiseError(JFException("")) },
            { just(it) }
        )
}

object BrokerBuyApi
{
    private data class BuyParameter(
        val label: String,
        val instrument: Instrument,
        val orderCommand: IEngine.OrderCommand,
        val amount: Double,
        val slPrice: Double,
        val price: Double,
        val slippage: Double,
        val comment: String
    )

    fun <F> QuoteDependencies<F>.brokerBuy(
        assetName: String,
        contracts: Int,
        slDistance: Double,
        limitPrice: Double,
        slippage: Double,
        orderText: String
    ): Kind<F, BrokerBuyResult> =
        fromAssetName(assetName)
            .flatMap { instrument ->
                logger.debug("$instrument is tradeable ${instrument.isTradable}")
                logger.debug("$instrument is tradeable via engine ${jfContext.engine.isTradable(instrument)}")
                filterTradeable(instrument)
            }
            .flatMap { instrument ->
                createBuyParameter(
                    instrument = instrument,
                    contracts = contracts,
                    slDistance = slDistance,
                    limitPrice = limitPrice,
                    slippage = slippage,
                    orderText = orderText
                )
            }
            .flatMap {
                logger.debug(
                    "Called BrokerBuy: assetName $assetName" +
                            " contracts $contracts " +
                            "slDistance $slDistance" +
                            " limitPrice $limitPrice" +
                            " isTradeable ${it.instrument.isTradable}"
                )
                submitOrder(it)
            }
            .flatMap { processOrderAndGetResult(it, slDistance, limitPrice != 0.0) }
            .handleError { error ->
                when (error)
                {
                    is AssetNotTradeableException -> natives.jcallback_BrokerError("Asset $assetName currently not tradeable!")
                    is TimeoutException -> natives.jcallback_BrokerError("BrokerBuy timeout!")
                    else -> logger.error("BrokerBuy failed! Error: $error Stack trace: ${getStackTrace(error)}")
                }
                val errorCode = if (error is TimeoutException) BROKER_BUY_TIMEOUT else BROKER_BUY_FAIL
                BrokerBuyFailure(errorCode)
            }

    private fun <F> QuoteDependencies<F>.createBuyParameter(
        instrument: Instrument,
        contracts: Int,
        slDistance: Double,
        limitPrice: Double,
        slippage: Double,
        orderText: String
    ): Kind<F, BuyParameter> = binding {
        val label = createLabel().bind()
        val isLimitOrder = limitPrice != 0.0
        val orderCommand = createOrderCommand(contracts, isLimitOrder)
        val amount = contracts.toAmount()
        val roundedLimitPrice = createLimitPrice(instrument, limitPrice)
        val slPrice = createSLPrice(slDistance, instrument, orderCommand, limitPrice)

        val buyParameter = BuyParameter(
            label = label,
            instrument = instrument,
            orderCommand = orderCommand,
            amount = amount,
            slPrice = slPrice,
            price = roundedLimitPrice,
            slippage = slippage,
            comment = orderText
        )
        logger.debug("BuyParameter: $buyParameter")
        buyParameter
    }

    private fun <F> QuoteDependencies<F>.submitOrder(buyParameter: BuyParameter): Kind<F, IOrder> =
        invoke {
            engine
                .submit(
                    label = buyParameter.label,
                    instrument = buyParameter.instrument,
                    orderCommand = buyParameter.orderCommand,
                    amount = buyParameter.amount,
                    stopLossPrice = buyParameter.slPrice,
                    price = buyParameter.price,
                    slippage = buyParameter.slippage,
                    comment = buyParameter.comment
                )
                .filter {
                    if (buyParameter.price != 0.0) it.type == OrderEventType.SUBMIT_OK
                    else it.type == OrderEventType.FULLY_FILLED
                }
                .timeout(pluginSettings.maxSecondsForOrderBuy(), TimeUnit.SECONDS)
                .map { it.order }
                .blockingFirst()
        }

    fun <F> QuoteDependencies<F>.createLabel(): Kind<F, String> =
        invoke { pluginSettings.labelPrefix() + System.currentTimeMillis().toString() }

    fun createOrderCommand(contracts: Int, isLimitOrder: Boolean): IEngine.OrderCommand =
        when
        {
            contracts >= 0 && !isLimitOrder -> IEngine.OrderCommand.BUY
            contracts >= 0 && isLimitOrder -> IEngine.OrderCommand.BUYLIMIT
            contracts < 0 && !isLimitOrder -> IEngine.OrderCommand.SELL
            else -> IEngine.OrderCommand.SELLLIMIT
        }

    fun createLimitPrice(instrument: Instrument, limitPrice: Double) =
        if (limitPrice != 0.0) Price(instrument, limitPrice).toDouble() else 0.0

    fun <F> QuoteDependencies<F>.createSLPrice(
        slDistance: Double,
        instrument: Instrument,
        orderCommand: IEngine.OrderCommand,
        limitPrice: Double
    ): Double
    {
        if (slDistance <= 0) return TradingSettings.noSLPrice
        val openPrice = if (limitPrice != 0.0) limitPrice
        else if (orderCommand == IEngine.OrderCommand.BUY) getAsk(instrument) else getBid(instrument)
        val slPrice = if (orderCommand == IEngine.OrderCommand.BUY) openPrice - slDistance
        else openPrice + slDistance
        return slPrice.asPrice(instrument)
    }

    fun <F> QuoteDependencies<F>.processOrderAndGetResult(
        order: IOrder,
        slDistance: Double,
        isLimitOrder: Boolean
    ): Kind<F, BrokerBuyResult> =
        catch {
            val returnCode = if (order.state == if (isLimitOrder) IOrder.State.OPENED else IOrder.State.FILLED)
            {
                if (slDistance == -1.0) BROKER_BUY_OPPOSITE_CLOSE else order.zorroId()
            } else BROKER_BUY_FAIL
            BrokerBuySuccess(returnCode, order.openPrice)
        }
}
