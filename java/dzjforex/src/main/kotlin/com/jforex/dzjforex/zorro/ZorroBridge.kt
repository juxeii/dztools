package com.jforex.dzjforex.zorro

import arrow.effects.DeferredK
import arrow.effects.fix
import com.jforex.dzjforex.account.BrokerAccountApi.brokerAccount
import com.jforex.dzjforex.asset.BrokerAssetApi.brokerAsset
import com.jforex.dzjforex.asset.createBrokerAssetApi
import com.jforex.dzjforex.buy.BrokerBuyApi
import com.jforex.dzjforex.buy.BrokerBuyApi.brokerBuy
import com.jforex.dzjforex.buy.createBrokerBuyApi
import com.jforex.dzjforex.history.BrokerHistoryApi.brokerHistory
import com.jforex.dzjforex.login.LoginApi.create
import com.jforex.dzjforex.login.LoginApi.logout
import com.jforex.dzjforex.login.loginApi
import com.jforex.dzjforex.misc.PluginApi.isConnected
import com.jforex.dzjforex.misc.PluginApi.progressWait
import com.jforex.dzjforex.misc.contextApi
import com.jforex.dzjforex.misc.logger
import com.jforex.dzjforex.misc.pluginApi
import com.jforex.dzjforex.sell.BrokerSellApi
import com.jforex.dzjforex.sell.BrokerSellApi.brokerSell
import com.jforex.dzjforex.stop.BrokerStopApi.setSL
import com.jforex.dzjforex.subscription.BrokerSubscribeApi.subscribeInstrument
import com.jforex.dzjforex.subscription.createBrokerSubscribeApi
import com.jforex.dzjforex.time.BrokerTimeApi.brokerTime
import com.jforex.dzjforex.trade.BrokerTradeApi.create
import com.jforex.dzjforex.trade.createBrokerTradeApi
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset

class ZorroBridge
{
    fun doLogin(
        username: String,
        password: String,
        accountType: String,
        out_AccountNamesToFill: Array<String>
    ): Int
    {
        val loginTask = DeferredK {
            loginApi
                .create(username, password, accountType, out_AccountNamesToFill)
                .fix()
                .unsafeRunSync()
        }
        return pluginApi.progressWait(loginTask)
    }

    fun doLogout() =
        loginApi
            .logout()
            .fix()
            .unsafeRunSync()

    fun doBrokerTime(out_ServerTimeToFill: DoubleArray):Int {
        val result =  contextApi
            .brokerTime(out_ServerTimeToFill)
            .fix()
            .unsafeRunSync()
        logger.debug("doBrokerTime result is $result")
        return result
    }

    fun doSubscribeAsset(assetName: String): Int
    {
        val subscribeTask = DeferredK {
            createBrokerSubscribeApi().run {
                subscribeInstrument(assetName)
                    .fix()
                    .unsafeRunSync()
            }
        }
        val result= pluginApi.progressWait(subscribeTask)
        logger.debug("doSubscribeAsset result is $result")
        return result
    }

    fun doBrokerAsset(assetName: String, out_AssetParamsToFill: DoubleArray): Int
    {
        val result = createBrokerAssetApi()
            .brokerAsset(assetName, out_AssetParamsToFill)
            .fix()
            .unsafeRunSync()
        logger.debug("doBrokerAsset result is $result")
        return result
    }


    fun doBrokerAccount(out_AccountInfoToFill: DoubleArray): Int
    {
        val result = contextApi
            .brokerAccount(out_AccountInfoToFill)
            .fix()
            .unsafeRunSync()
        logger.debug("doBrokerAccount result is $result")
        return result
    }


    fun doBrokerTrade(orderId: Int, out_TradeInfoToFill: DoubleArray): Int
    {
        val tradeResult = createBrokerTradeApi()
            .create(orderId, out_TradeInfoToFill)
            .fix()
            .unsafeRunSync()
        logger.debug("doBrokerTrade result is $tradeResult")
        return tradeResult
    }


    fun doBrokerBuy2(
        assetName: String,
        contracts: Int,
        slDistance: Double,
        limit: Double,
        out_TradeInfoToFill: DoubleArray
    ): Int
    {
        val buyTask = DeferredK {
            createBrokerBuyApi()
                .brokerBuy(assetName, contracts, slDistance, limit, out_TradeInfoToFill)
                .fix()
                .unsafeRunSync()
        }
        val buyResult = pluginApi.progressWait(buyTask)
        logger.debug("doBrokerBuy2 result is $buyResult")
        return buyResult
    }

    fun doBrokerSell(
        orderId: Int,
        contracts: Int
    ): Int
    {
        val sellTask = DeferredK {
            contextApi
                .brokerSell(orderId, contracts)
                .fix()
                .unsafeRunSync()
        }
        return pluginApi.progressWait(sellTask)
    }

    fun doBrokerStop(
        orderId: Int,
        slPrice: Double
    ): Int
    {
        val stopTask = DeferredK {
            contextApi
                .setSL(orderId, slPrice)
                .fix()
                .unsafeRunSync()
        }
        return pluginApi.progressWait(stopTask)
    }

    fun doBrokerHistory2(
        assetName: String,
        utcStartDate: Double,
        utcEndDate: Double,
        periodInMinutes: Int,
        noOfTicks: Int,
        out_TickInfoToFill: DoubleArray
    ): Int
    {
        val historyTask = DeferredK {
            contextApi
                .brokerHistory(assetName, utcStartDate, utcEndDate, periodInMinutes, noOfTicks, out_TickInfoToFill)
                .fix()
                .unsafeRunSync()
        }
        return pluginApi.progressWait(historyTask)
    }

    private fun brokerCommandStringReturn(string: String, bytes: ByteArray): Double
    {
        val buf = ByteBuffer.wrap(bytes)
        val stringAsBytes = string.toByteArray(Charset.forName("UTF-8"))
        buf.put(stringAsBytes)
        buf.putInt(0)
        buf.flip()
        return BROKER_COMMAND_OK
    }

    private fun brokerCommandGetDouble(bytes: ByteArray) =
        ByteBuffer
            .wrap(bytes)
            .order(ByteOrder.LITTLE_ENDIAN)
            .double

    fun doBrokerCommand(commandId: Int, bytes: ByteArray): Double
    {
        return if (!pluginApi.isConnected()) BROKER_COMMAND_UNAVAILABLE
        else when (commandId)
        {
            SET_ORDERTEXT ->
            {
                val orderText = String(bytes)
                BrokerBuyApi.setOrderText(orderText)
                logger.debug("doBrokerCommand SET_ORDERTEXT called with ordertext $orderText")
                BROKER_COMMAND_OK
            }
            SET_LIMIT ->
            {
                val limitPrice = brokerCommandGetDouble(bytes)
                BrokerSellApi.setLmitPrice(limitPrice)
                logger.debug("doBrokerCommand SET_LIMIT called with limitPrice $limitPrice")
                BROKER_COMMAND_OK
            }
            GET_ACCOUNT ->
            {
                logger.debug("doBrokerCommand GET_ACCOUNT called")
                brokerCommandStringReturn(contextApi.account.accountId, bytes)
            }
            else -> BROKER_COMMAND_UNAVAILABLE
        }
    }
}
