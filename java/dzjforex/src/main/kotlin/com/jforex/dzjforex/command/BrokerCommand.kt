package com.jforex.dzjforex.command

import arrow.effects.instances.io.applicativeError.handleError
import arrow.effects.instances.io.monad.map
import com.dukascopy.api.Instrument
import com.jforex.dzjforex.buy.BrokerBuyApi
import com.jforex.dzjforex.misc.InstrumentApi.fromAssetName
import com.jforex.dzjforex.misc.contextApi
import com.jforex.dzjforex.misc.createQuoteProviderApi
import com.jforex.dzjforex.misc.logger
import com.jforex.dzjforex.misc.runDirect
import com.jforex.dzjforex.sell.BrokerSellApi
import com.jforex.dzjforex.time.toDATEFormat
import com.jforex.dzjforex.zorro.*
import com.jforex.kforexutils.instrument.noOfDecimalPlaces
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset

typealias CommandCall = (ByteArray) -> Double

val commandbyId = mapOf<Int, CommandCall>(
    SET_ORDERTEXT to ::setOrderText,
    SET_LIMIT to ::setLimit,
    GET_ACCOUNT to ::getAccount,
    GET_TIME to ::getTime,
    GET_DIGITS to ::getDigits,
    GET_TRADEALLOWED to ::getTradeAllowed,
    GET_MINLOT to ::getMinLot,
    GET_MAXLOT to ::getMaxLot
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

fun setLimit(bytes: ByteArray): Double
{
    val limitPrice = brokerCommandGetDouble(bytes)
    BrokerSellApi.setLmitPrice(limitPrice)
    logger.debug("doBrokerCommand SET_LIMIT called with limitPrice $limitPrice")
    return BROKER_COMMAND_OK
}

fun getAccount(bytes: ByteArray): Double
{
    logger.debug("doBrokerCommand GET_ACCOUNT called")
    return brokerCommandStringReturn(contextApi.account.accountId, bytes)
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

fun setOrderText(bytes: ByteArray): Double
{
    val orderText = String(bytes)
    BrokerBuyApi.setOrderText(orderText)
    logger.debug("doBrokerCommand SET_ORDERTEXT called with ordertext $orderText")
    return BROKER_COMMAND_OK
}