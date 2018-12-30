package com.jforex.dzjforex.command

import arrow.core.None
import arrow.core.Option
import arrow.core.some
import arrow.effects.instances.io.applicativeError.handleError
import arrow.effects.instances.io.monad.map
import com.dukascopy.api.Instrument
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jforex.dzjforex.asset.BrokerAssetApi.getMarginCost
import com.jforex.dzjforex.asset.createBrokerAssetApi
import com.jforex.dzjforex.misc.InstrumentApi.fromAssetName
import com.jforex.dzjforex.misc.contextApi
import com.jforex.dzjforex.misc.createQuoteProviderApi
import com.jforex.dzjforex.misc.logger
import com.jforex.dzjforex.misc.runDirect
import com.jforex.dzjforex.time.toDATEFormat
import com.jforex.dzjforex.zorro.*
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

val commandbyId = mapOf<Int, CommandCall>(
    SET_ORDERTEXT to ::setOrderText,
    SET_LIMIT to ::setLimit,
    SET_SLIPPAGE to ::setSlippage,
    GET_ACCOUNT to ::getAccount,
    GET_TIME to ::getTime,
    GET_DIGITS to ::getDigits,
    GET_TRADEALLOWED to ::getTradeAllowed,
    GET_MINLOT to ::getMinLot,
    GET_MAXLOT to ::getMaxLot,
    GET_MAXTICKS to ::getMaxTicks,
    GET_MARGININIT to ::getMarginInit
)

fun brokerCommandStringReturn(string: String, bytes: ByteArray): Double
{
    val buf = ByteBuffer.wrap(bytes)
    val stringAsBytes = string.toByteArray(Charset.forName("UTF-8"))
    buf.put(stringAsBytes)
    buf.putInt(0)
    buf.flip()
    return BROKER_COMMAND_OK
}

fun getLittleEndianBuffer(bytes: ByteArray) =
    ByteBuffer
        .wrap(bytes)
        .order(ByteOrder.LITTLE_ENDIAN)

fun brokerCommandGetDouble(bytes: ByteArray) = getLittleEndianBuffer(bytes).double

fun brokerCommandGetInt(bytes: ByteArray) = getLittleEndianBuffer(bytes).int

fun brokerCommandGetSymbolData(
    bytes: ByteArray,
    dataProvider: (Instrument) -> Double
) = runDirect(
    contextApi
        .fromAssetName(String(bytes))
        .map(dataProvider)
        .handleError { BROKER_COMMAND_ERROR })

fun setSlippage(bytes: ByteArray): Double
{
    val slippage = Pips(brokerCommandGetInt(bytes).toDouble())
    bcSlippage.accept(slippage)
    return BROKER_COMMAND_OK
}

fun setLimit(bytes: ByteArray): Double
{
    val limitPrice = brokerCommandGetDouble(bytes)
    bcLimitPrice.accept(limitPrice.some())
    logger.debug("doBrokerCommand SET_LIMIT called with limitPrice $limitPrice")
    return BROKER_COMMAND_OK
}

fun getAccount(bytes: ByteArray): Double
{
    logger.debug("doBrokerCommand GET_ACCOUNT called")
    return brokerCommandStringReturn(contextApi.account.accountId, bytes)
}

fun getMaxTicks(bytes: ByteArray): Double
{
    logger.debug("doBrokerCommand GET_MAXTICKS called")
    return 1500.0
}

fun getTime(bytes: ByteArray): Double
{
    logger.debug("doBrokerCommand GET_TIME called")
    val maxtickTime = createQuoteProviderApi()
        .quotes
        .values
        .map { it.tick.time }
        .max()!!
    return toDATEFormat(maxtickTime)
}

fun getDigits(bytes: ByteArray): Double
{
    val symbol = String(bytes)
    logger.debug("doBrokerCommand GET_DIGITS called for symbol $symbol")
    return brokerCommandGetSymbolData(bytes) { it.noOfDecimalPlaces.toDouble() }
}

fun getTradeAllowed(bytes: ByteArray): Double
{
    val symbol = String(bytes)
    logger.debug("doBrokerCommand GET_TRADEALLOWED called for symbol $symbol")
    return brokerCommandGetSymbolData(bytes) { if (it.isTradable) 1.0 else 0.0 }
}

fun getMinLot(bytes: ByteArray): Double
{
    val symbol = String(bytes)
    logger.debug("doBrokerCommand GET_MINLOT called for symbol $symbol")
    return brokerCommandGetSymbolData(bytes) { it.minTradeAmount }
}

fun getMaxLot(bytes: ByteArray): Double
{
    val symbol = String(bytes)
    logger.debug("doBrokerCommand GET_MAXLOT called for symbol $symbol")
    return brokerCommandGetSymbolData(bytes) { it.maxTradeAmount }
}

fun getMarginInit(bytes: ByteArray): Double
{
    val symbol = String(bytes)
    logger.debug("doBrokerCommand GET_MARGININIT called for symbol $symbol")
    return brokerCommandGetSymbolData(bytes) { runDirect(createBrokerAssetApi().getMarginCost(it)) }
}

fun setOrderText(bytes: ByteArray): Double
{
    val orderText = String(bytes)
    bcOrderText.accept(orderText.some())
    logger.debug("doBrokerCommand SET_ORDERTEXT called with ordertext $orderText")
    return BROKER_COMMAND_OK
}