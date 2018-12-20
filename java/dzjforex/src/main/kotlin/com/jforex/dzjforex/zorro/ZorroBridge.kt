package com.jforex.dzjforex.zorro

import arrow.core.fix
import arrow.effects.DeferredK
import arrow.effects.fix
import com.jforex.dzjforex.asset.BrokerAssetApi.getAssetParams
import com.jforex.dzjforex.asset.createBrokerAssetApi
import com.jforex.dzjforex.login.LoginApi.create
import com.jforex.dzjforex.login.loginApi
import com.jforex.dzjforex.misc.*
import com.jforex.dzjforex.settings.PluginSettings
import com.jforex.dzjforex.subscription.BrokerSubscribeApi.subscribeInstrument
import com.jforex.dzjforex.subscription.BrokerSubscribeApi.waitForLatestQuotes
import com.jforex.dzjforex.subscription.createBrokerSubscribeApi
import com.jforex.dzjforex.time.BrokerTimeApi.create
import com.jforex.dzjforex.time.brokerTimeApi
import org.aeonbits.owner.ConfigFactory
import org.apache.logging.log4j.LogManager
import com.jforex.dzjforex.misc.PluginApi.progressWait

private val logger = LogManager.getLogger()

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

    fun doLogout(): Int
    {
        client.disconnect()
        return LOGOUT_OK
    }

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
                    .flatMap { waitForLatestQuotes(it) }
                    .map { latestQuotes -> latestQuotes.forEach { quote -> saveQuote(quote) } }
                    .fix()
                    .fold({ SUBSCRIBE_FAIL }) { SUBSCRIBE_OK }
            }
        }
        return pluginApi.progressWait(subscribeTask)
    }

    fun doBrokerAsset(
        assetName: String,
        out_AssetParamsToFill: DoubleArray
    ) = instrumentFromAssetName(assetName)
        .map { instrument -> createBrokerAssetApi(instrument).getAssetParams() }
        .map { assetParams ->
            out_AssetParamsToFill[0] = assetParams.price
            out_AssetParamsToFill[1] = assetParams.spread
        }
        .fold({ ASSET_UNAVAILABLE }) { ASSET_AVAILABLE }

    fun doBrokerAccount(accountInfoParams: DoubleArray): Int
    {
        return 42
    }

    fun doBrokerTrade(
        orderID: Int,
        tradeParams: DoubleArray
    ): Int
    {
        return 42
    }

    fun doBrokerBuy2(
        assetName: String,
        contracts: Int,
        slDistance: Double,
        limit: Double,
        tradeParams: DoubleArray
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
        tickParams: DoubleArray
    ): Int
    {
        return 42
    }

    fun doSetOrderText(orderText: String): Int
    {
        return 42
    }
}
