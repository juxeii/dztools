package com.jforex.dzjforex.command

import arrow.core.None
import arrow.core.Option
import arrow.core.some
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jforex.dzjforex.account.AccountApi.accountName
import com.jforex.dzjforex.asset.BrokerAssetApi.getMarginCost
import com.jforex.dzjforex.misc.ContextApi.getSubscribedInstruments
import com.jforex.dzjforex.misc.ContextDependencies
import com.jforex.dzjforex.misc.PluginApi.createInstrument
import com.jforex.dzjforex.misc.logger
import com.jforex.dzjforex.order.OrderLookupApi.getOpenZorroOrders
import com.jforex.dzjforex.order.OrderLookupApi.getOpenZorroOrdersForInstrument
import com.jforex.dzjforex.time.toUTCTime
import com.jforex.dzjforex.zorro.BROKER_COMMAND_ERROR
import com.jforex.dzjforex.zorro.BROKER_COMMAND_OK
import com.jforex.kforexutils.instrument.noOfDecimalPlaces
import com.jforex.kforexutils.instrument.tick
import com.jforex.kforexutils.price.Pips

object BrokerCommandApi
{
    private val bcSlippage: BehaviorRelay<Pips> = BehaviorRelay.createDefault(Pips(5.0))
    private val bcLimitPrice: BehaviorRelay<Option<Double>> = BehaviorRelay.createDefault(None)
    private val bcOrderText: BehaviorRelay<String> = BehaviorRelay.createDefault("")
    private val bcServerState: BehaviorRelay<ServerState> = BehaviorRelay.createDefault(ServerState.DISCONNECTED)

    enum class ServerState(val value: Int)
    {
        CONNECTED(1),
        DISCONNECTED(2),
        TEMPORARILY_DISCONNECTED(3);

        fun toDouble() = value.toDouble()
    }

    fun getBcSlippage() = bcSlippage.value!!.toDouble()

    fun maybeBcLimitPrice() = bcLimitPrice.value!!

    fun getBcOrderText() = bcOrderText.value!!

    fun getBcServerState() = bcServerState.value!!

    fun <F> ContextDependencies<F>.setSlippage(slippage: Double) = catch {
        bcSlippage.accept(Pips(slippage))
        logger.debug("Broker command SET_SLIPPAGE called with slippage $slippage")
        BROKER_COMMAND_OK
    }

    fun <F> ContextDependencies<F>.setLimit(limit: Double) = catch {
        bcLimitPrice.accept(limit.some())
        logger.debug("Broker command SET_LIMIT called with limit $limit")
        BROKER_COMMAND_OK
    }

    fun <F> ContextDependencies<F>.setOrderText(orderText: String) = catch {
        bcOrderText.accept(orderText)
        logger.debug("Broker command SET_ORDERTEXT called with ordertext $orderText")
        BROKER_COMMAND_OK
    }

    fun <F> ContextDependencies<F>.getAccount() = catch {
        logger.debug("Broker command GET_ACCOUNT called")
        BrokerCommandData(BROKER_COMMAND_OK.toInt(), accountName())
    }

    fun <F> ContextDependencies<F>.getMaxTicks() = catch {
        logger.debug("Broker command GET_MAXTICKS called")
        pluginSettings.maxTicks().toDouble()
    }

    fun <F> ContextDependencies<F>.getTime() = bindingCatch {
        logger.debug("Broker command GET_TIME called")
        getSubscribedInstruments()
            .bind()
            .map { it.tick().time }
            .max()!!
            .toUTCTime()
    }

    fun <F> ContextDependencies<F>.getServerState() = catch {
        logger.debug("Broker command GET_SERVERSTATE called")
        if (client.isConnected)
        {
            val serverState = if (getBcServerState() == ServerState.DISCONNECTED)
                ServerState.TEMPORARILY_DISCONNECTED.toDouble()
            else
                ServerState.CONNECTED.toDouble()
            bcServerState.accept(ServerState.CONNECTED)
            serverState
        } else
        {
            bcServerState.accept(ServerState.DISCONNECTED)
            ServerState.DISCONNECTED.toDouble()
        }
    }

    fun <F> ContextDependencies<F>.getNTrades() = bindingCatch {
        logger.debug("Broker command GET_NTRADES called.")
        getOpenZorroOrders()
            .bind()
            .size
            .toDouble()
    }

    fun <F> ContextDependencies<F>.getDigits(assetName: String) = bindingCatch {
        logger.debug("Broker command GET_DIGITS called for asset $assetName")
        createInstrument(assetName)
            .map { it.noOfDecimalPlaces.toDouble() }
            .bind()
    }

    fun <F> ContextDependencies<F>.getTradeAllowed(assetName: String) = bindingCatch {
        logger.debug("Broker command GET_TRADEALLOWED called for asset $assetName")
        createInstrument(assetName)
            .map { if (it.isTradable) 1.0 else 0.0 }
            .bind()
    }

    fun <F> ContextDependencies<F>.getMinLot(assetName: String) = bindingCatch {
        logger.debug("Broker command GET_MINLOT called for asset $assetName")
        createInstrument(assetName)
            .map { it.minTradeAmount }
            .bind()
    }

    fun <F> ContextDependencies<F>.getMaxLot(assetName: String) = bindingCatch {
        logger.debug("Broker command GET_MAXLOT called for asset $assetName")
        createInstrument(assetName)
            .map { it.maxTradeAmount }
            .bind()
    }

    fun <F> ContextDependencies<F>.getMarginInit(assetName: String) = bindingCatch {
        logger.debug("Broker command GET_MARGININIT called for asset $assetName")
        createInstrument(assetName)
            .flatMap { getMarginCost(it) }
            .bind()
    }

    fun <F> ContextDependencies<F>.getPosition(assetName: String) = bindingCatch {
        logger.debug("Broker command GET_POSITION called for asset $assetName")
        createInstrument(assetName)
            .flatMap { getOpenZorroOrdersForInstrument(it) }
            .bind()
            .stream()
            .mapToInt { it.toSignedContracts() }
            .sum()
            .toDouble()
    }
}