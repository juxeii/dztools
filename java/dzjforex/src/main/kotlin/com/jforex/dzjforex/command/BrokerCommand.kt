package com.jforex.dzjforex.command

import arrow.Kind
import arrow.core.None
import arrow.core.Option
import arrow.core.some
import arrow.typeclasses.bindingCatch
import com.dukascopy.api.Instrument
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jforex.dzjforex.asset.BrokerAssetApi.getMarginCost
import com.jforex.dzjforex.asset.createBrokerAssetApi
import com.jforex.dzjforex.misc.ContextDependencies
import com.jforex.dzjforex.misc.InstrumentApi.fromAssetName
import com.jforex.dzjforex.misc.QuoteDependencies
import com.jforex.dzjforex.misc.logger
import com.jforex.dzjforex.misc.runDirect
import com.jforex.dzjforex.time.toUTCTime
import com.jforex.dzjforex.zorro.BROKER_COMMAND_ERROR
import com.jforex.dzjforex.zorro.BROKER_COMMAND_OK
import com.jforex.kforexutils.instrument.noOfDecimalPlaces
import com.jforex.kforexutils.price.Pips
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset

typealias CommandCall = (ByteArray) -> Double

val bcSlippage: BehaviorRelay<Pips> = BehaviorRelay.createDefault(Pips(5.0))
fun getBcSlippage() = bcSlippage.value!!.toDouble()

val bcLimitPrice: BehaviorRelay<Option<Double>> = BehaviorRelay.createDefault(None)
fun maybeBcLimitPrice() = bcLimitPrice.value!!

val bcOrderText: BehaviorRelay<Option<String>> = BehaviorRelay.createDefault(None)
fun maybeBcOrderText() = bcOrderText.value!!

object BrokerCommandApi
{
    fun <F> ContextDependencies<F>.brokerCommandStringReturn(string: String, bytes: ByteArray): Kind<F, Double> =
        catch {
            val buf = ByteBuffer.wrap(bytes)
            val stringAsBytes = string.toByteArray(Charset.forName("UTF-8"))
            buf.put(stringAsBytes)
            buf.putInt(0)
            buf.flip()
            BROKER_COMMAND_OK
        }

    fun <F> ContextDependencies<F>.getLittleEndianBuffer(bytes: ByteArray): Kind<F, ByteBuffer> = invoke {
        ByteBuffer
            .wrap(bytes)
            .order(ByteOrder.LITTLE_ENDIAN)
    }

    fun <F> ContextDependencies<F>.brokerCommandGetDouble(bytes: ByteArray) =
        getLittleEndianBuffer(bytes).map { it.double }

    fun <F> ContextDependencies<F>.brokerCommandGetInt(bytes: ByteArray) =
        getLittleEndianBuffer(bytes).map { it.int }

    fun <F> ContextDependencies<F>.brokerCommandGetSymbolData(
        bytes: ByteArray,
        dataProvider: (Instrument) -> Double
    ): Kind<F, Double> = fromAssetName(String(bytes))
        .map(dataProvider)
        .handleError { BROKER_COMMAND_ERROR }

    fun <F> ContextDependencies<F>.setSlippage(bytes: ByteArray): Kind<F, Double> = bindingCatch {
        val slippage = Pips(brokerCommandGetInt(bytes).bind().toDouble())
        bcSlippage.accept(slippage)
        logger.debug("doBrokerCommand SET_SLIPPAGE called with slippage $slippage")
        BROKER_COMMAND_OK
    }

    fun <F> ContextDependencies<F>.setLimit(bytes: ByteArray): Kind<F, Double> = bindingCatch {
        val limitPrice = brokerCommandGetDouble(bytes).bind()
        bcLimitPrice.accept(limitPrice.some())
        logger.debug("doBrokerCommand SET_LIMIT called with limitPrice $limitPrice")
        BROKER_COMMAND_OK
    }

    fun <F> ContextDependencies<F>.setOrderText(bytes: ByteArray): Kind<F, Double> = invoke {
        val orderText = String(bytes)
        bcOrderText.accept(orderText.some())
        logger.debug("doBrokerCommand SET_ORDERTEXT called with ordertext $orderText")
        BROKER_COMMAND_OK
    }

    fun <F> ContextDependencies<F>.getAccount(bytes: ByteArray): Kind<F, Double> =
        brokerCommandStringReturn(account.accountId, bytes)

    fun <F> ContextDependencies<F>.getMaxTicks(): Kind<F, Double> = just(pluginSettings.maxTicks().toDouble())

    fun <F> QuoteDependencies<F>.getTime(): Kind<F, Double> = invoke {
        logger.debug("doBrokerCommand GET_TIME called")
        quotes
            .values
            .map { it.tick.time }
            .max()!!
            .toUTCTime()
    }

    fun <F> ContextDependencies<F>.getDigits(bytes: ByteArray): Kind<F, Double> = bindingCatch {
        val symbol = String(bytes)
        logger.debug("doBrokerCommand GET_DIGITS called for symbol $symbol")
        brokerCommandGetSymbolData(bytes) { it.noOfDecimalPlaces.toDouble() }.bind()
    }

    fun <F> ContextDependencies<F>.getTradeAllowed(bytes: ByteArray): Kind<F, Double> = bindingCatch {
        val symbol = String(bytes)
        logger.debug("doBrokerCommand GET_TRADEALLOWED called for symbol $symbol")
        brokerCommandGetSymbolData(bytes) {
            if (it.isTradable) 1.0 else 0.0
        }.bind()
    }

    fun <F> ContextDependencies<F>.getMinLot(bytes: ByteArray): Kind<F, Double> = bindingCatch {
        val symbol = String(bytes)
        logger.debug("doBrokerCommand GET_MINLOT called for symbol $symbol")
        brokerCommandGetSymbolData(bytes) { it.minTradeAmount }.bind()
    }

    fun <F> ContextDependencies<F>.getMaxLot(bytes: ByteArray): Kind<F, Double> = bindingCatch {
        val symbol = String(bytes)
        logger.debug("doBrokerCommand GET_MINLOT called for symbol $symbol")
        brokerCommandGetSymbolData(bytes) { it.maxTradeAmount }.bind()
    }

    fun <F> ContextDependencies<F>.getMarginInit(bytes: ByteArray): Kind<F, Double> = bindingCatch {
        val symbol = String(bytes)
        logger.debug("doBrokerCommand GET_MARGININIT called for symbol $symbol")
        brokerCommandGetSymbolData(bytes) { runDirect(createBrokerAssetApi().getMarginCost(it)) }.bind()
    }
}