package com.jforex.dzjforex.zorro

import arrow.core.fix
import arrow.effects.DeferredK
import arrow.effects.fix
import com.dukascopy.api.system.IClient
import com.jforex.dzjforex.asset.BrokerAssetApi.create
import com.jforex.dzjforex.asset.createBrokerAssetApi
import com.jforex.dzjforex.login.LoginApi.create
import com.jforex.dzjforex.login.LoginApi.logout
import com.jforex.dzjforex.login.loginApi
import com.jforex.dzjforex.misc.PluginApi.progressWait
import com.jforex.dzjforex.misc.getClient
import com.jforex.dzjforex.misc.pluginApi
import com.jforex.dzjforex.subscription.BrokerSubscribeApi.subscribeInstrument
import com.jforex.dzjforex.subscription.createBrokerSubscribeApi
import com.jforex.dzjforex.time.BrokerTimeApi.create
import com.jforex.dzjforex.time.brokerTimeApi

val client: IClient = getClient()

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
                    .fold({ SUBSCRIBE_FAIL }) { SUBSCRIBE_OK }
            }
        }
        return pluginApi.progressWait(subscribeTask)
    }

    fun doBrokerAsset(assetName: String, out_AssetParamsToFill: DoubleArray) =
        createBrokerAssetApi()
            .create(assetName, out_AssetParamsToFill)
            .fix()
            .unsafeRunSync()

    fun doBrokerAccount(out_AccountInfoToFill: DoubleArray): Int
    {
        return 42
    }

    fun doBrokerTrade(
        orderID: Int,
        out_TradeInfoToFill: DoubleArray
    ): Int
    {
        return 42
    }

    fun doBrokerBuy2(
        assetName: String,
        contracts: Int,
        slDistance: Double,
        limit: Double,
        out_TradeInfoToFill: DoubleArray
    ): Int
    {
        return 42
    }

    fun doBrokerSell(
        orderID: Int,
        contracts: Int
    ): Int
    {
        return 42
    }

    fun doBrokerStop(
        orderID: Int,
        slPrice: Double
    ): Int
    {
        return 42
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

    fun doSetOrderText(orderText: String): Int
    {
        return 42
    }
}
