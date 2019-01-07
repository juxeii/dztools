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
import com.jforex.dzjforex.time.toUTCTime
import com.jforex.dzjforex.zorro.BROKER_COMMAND_OK
import com.jforex.kforexutils.instrument.noOfDecimalPlaces
import com.jforex.kforexutils.instrument.tick
import com.jforex.kforexutils.price.Pips

val bcSlippage: BehaviorRelay<Pips> = BehaviorRelay.createDefault(Pips(5.0))
fun getBcSlippage() = bcSlippage.value!!.toDouble()

val bcLimitPrice: BehaviorRelay<Option<Double>> = BehaviorRelay.createDefault(None)
fun maybeBcLimitPrice() = bcLimitPrice.value!!

val bcOrderText: BehaviorRelay<String> = BehaviorRelay.createDefault("")
fun getBcOrderText() = bcOrderText.value!!

val bcServerState: BehaviorRelay<ServerState> = BehaviorRelay.createDefault(ServerState.DISCONNECTED)
fun getBcServerState() = bcServerState.value!!

enum class ServerState(val value: Int) {
    CONNECTED(1),
    DISCONNECTED(2),
    TEMPORARILY_DISCONNECTED(3);

    fun toDouble() = value.toDouble()
}

object BrokerCommandApi {
    fun <F> ContextDependencies<F>.setSlippage(slippage: Double) = bindingCatch {
        bcSlippage.accept(Pips(slippage))
        logger.debug("Broker command SET_SLIPPAGE called with slippage $slippage")
        BROKER_COMMAND_OK
    }

    fun <F> ContextDependencies<F>.setLimit(limit: Double) = bindingCatch {
        bcLimitPrice.accept(limit.some())
        logger.debug("Broker command SET_LIMIT called with limit $limit")
        BROKER_COMMAND_OK
    }

    fun <F> ContextDependencies<F>.setOrderText(orderText: String) = delay {
        bcOrderText.accept(orderText)
        logger.debug("Broker command SET_ORDERTEXT called with ordertext $orderText")
        BROKER_COMMAND_OK
    }

    fun <F> ContextDependencies<F>.getAccount() = bindingCatch {
        BrokerCommandData(BROKER_COMMAND_OK.toInt(), accountName().bind())
    }

    fun <F> ContextDependencies<F>.getMaxTicks() = just(pluginSettings.maxTicks().toDouble())

    fun <F> ContextDependencies<F>.getTime() = binding {
        logger.debug("Broker command GET_TIME called")
        getSubscribedInstruments()
            .bind()
            .map { it.tick().time }
            .max()!!
            .toUTCTime()
    }

    fun <F> ContextDependencies<F>.getServerState() = binding {
        logger.debug("Broker command GET_SERVERSTATE called")
        if (client.isConnected) {
            bcServerState.accept(ServerState.CONNECTED)
            if (getBcServerState() == ServerState.DISCONNECTED)
                ServerState.TEMPORARILY_DISCONNECTED.toDouble()
            else
                ServerState.CONNECTED.toDouble()
        } else {
            bcServerState.accept(ServerState.DISCONNECTED)
            ServerState.DISCONNECTED.toDouble()
        }
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
}