package com.jforex.dzjforex.zorro

import arrow.core.some
import arrow.effects.DeferredK
import arrow.effects.fix
import com.jforex.dzjforex.account.BrokerAccountApi.create
import com.jforex.dzjforex.account.accountApi
import com.jforex.dzjforex.account.brokerAccountApi
import com.jforex.dzjforex.asset.BrokerAssetApi.create
import com.jforex.dzjforex.asset.createBrokerAssetApi
import com.jforex.dzjforex.buy.BrokerBuyApi.create
import com.jforex.dzjforex.buy.brokerBuyApi
import com.jforex.dzjforex.login.LoginApi.create
import com.jforex.dzjforex.login.LoginApi.logout
import com.jforex.dzjforex.login.loginApi
import com.jforex.dzjforex.misc.PluginApi.progressWait
import com.jforex.dzjforex.misc.logger
import com.jforex.dzjforex.misc.pluginApi
import com.jforex.dzjforex.sell.BrokerSellApi.create
import com.jforex.dzjforex.sell.bcLimitPrice
import com.jforex.dzjforex.sell.brokerSellApi
import com.jforex.dzjforex.stop.BrokerStopApi.create
import com.jforex.dzjforex.stop.brokerStopApi
import com.jforex.dzjforex.subscription.BrokerSubscribeApi.subscribeInstrument
import com.jforex.dzjforex.subscription.createBrokerSubscribeApi
import com.jforex.dzjforex.time.BrokerTimeApi.create
import com.jforex.dzjforex.time.brokerTimeApi
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

    fun doLogout() = loginApi.logout()

    fun doBrokerTime(out_ServerTimeToFill: DoubleArray) =
        brokerTimeApi
            .create(out_ServerTimeToFill)
            .fix()
            .unsafeRunSync()

    fun doSubscribeAsset(assetName: String): Int
    {
        val subscribeTask = DeferredK {
            createBrokerSubscribeApi().run {
                subscribeInstrument(assetName)
                    .fix()
                    .unsafeRunSync()
            }
        }
        return pluginApi.progressWait(subscribeTask)
    }

    fun doBrokerAsset(assetName: String, out_AssetParamsToFill: DoubleArray) =
        createBrokerAssetApi()
            .create(assetName, out_AssetParamsToFill)
            .fix()
            .unsafeRunSync()

    fun doBrokerAccount(out_AccountInfoToFill: DoubleArray) =
        brokerAccountApi
            .create(out_AccountInfoToFill)
            .fix()
            .unsafeRunSync()

    fun doBrokerTrade(orderId: Int, out_TradeInfoToFill: DoubleArray) =
        createBrokerTradeApi()
            .create(orderId, out_TradeInfoToFill)
            .fix()
            .unsafeRunSync()

    fun doBrokerBuy2(
        assetName: String,
        contracts: Int,
        slDistance: Double,
        limit: Double,
        out_TradeInfoToFill: DoubleArray
    ): Int
    {
        val buyTask = DeferredK {
            brokerBuyApi
                .create(assetName, contracts, slDistance, limit, out_TradeInfoToFill)
                .fix()
                .unsafeRunSync()
        }
        return pluginApi.progressWait(buyTask)
    }

    fun doBrokerSell(
        orderId: Int,
        contracts: Int
    ): Int
    {
        val sellTask = DeferredK {
            brokerSellApi
                .create(orderId, contracts)
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
            brokerStopApi
                .create(orderId, slPrice)
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
        return 42
    }

    fun brokerCommandStringReturn(string: String, bytes: ByteArray): Double
    {
        val buf = ByteBuffer.wrap(bytes)
        val stringAsBytes = string.toByteArray(Charset.forName("UTF-8"))
        buf.put(stringAsBytes)
        buf.putInt(0)
        buf.flip()
        return BROKER_COMMAND_OK
    }

    fun brokerCommandGetDouble(bytes: ByteArray) =
        ByteBuffer
            .wrap(bytes)
            .order(ByteOrder.LITTLE_ENDIAN)
            .double

    fun doBrokerCommand(commandId: Int, bytes: ByteArray): Double =
        when (commandId)
        {
            SET_ORDERTEXT ->
            {
                val orderText = String(bytes)
                logger.debug("doBrokerCommand SET_ORDERTEXT called with ordertext $orderText")
                BROKER_COMMAND_OK
            }
            SET_LIMIT ->
            {
                val limitPrice = brokerCommandGetDouble(bytes)
                bcLimitPrice.accept(limitPrice.some())
                logger.debug("doBrokerCommand SET_LIMIT called with limitPrice $limitPrice")
                BROKER_COMMAND_OK
            }
            GET_ACCOUNT ->{
                logger.debug("doBrokerCommand GET_ACCOUNT called")
                brokerCommandStringReturn(accountApi.account.accountId, bytes)
            }
            else -> BROKER_COMMAND_UNAVAILABLE
        }
}
