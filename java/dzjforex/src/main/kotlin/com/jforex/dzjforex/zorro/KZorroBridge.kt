package com.jforex.dzjforex.zorro

import arrow.data.runId
import com.jforex.dzjforex.asset.getAssetData
import com.jforex.dzjforex.login.LoginData
import com.jforex.dzjforex.login.loginToDukascopy
import com.jforex.dzjforex.login.logoutFromDukascopy
import com.jforex.dzjforex.misc.PluginEnvironment
import com.jforex.dzjforex.misc.PluginStrategy
import com.jforex.dzjforex.misc.getClient
import com.jforex.dzjforex.settings.PluginSettings
import com.jforex.dzjforex.subscription.subscribeAsset
import com.jforex.dzjforex.time.getServerTime
import org.aeonbits.owner.ConfigFactory
import org.apache.logging.log4j.LogManager

class KZorroBridge
{
    private val client = getClient()
    private val pluginSettings = ConfigFactory.create(PluginSettings::class.java)
    private val zCommunication = ZorroCommunication(pluginSettings)
    private val pluginStrategy = PluginStrategy(client, pluginSettings)
    private val environment = PluginEnvironment(client, pluginStrategy, pluginSettings)

    private val logger = LogManager.getLogger(KZorroBridge::class.java)

    fun doLogin(
        username: String,
        password: String,
        accountType: String,
        out_AccountNames: Array<String>
    ): Int
    {
        val loginData = LoginData(
            username,
            password,
            accountType
        )
        val loginTask = loginToDukascopy(loginData)
            .runId(client)
            .map { loginResult ->
                if (loginResult == LOGIN_OK)
                {
                    pluginStrategy.start(out_AccountNames)
                }
                loginResult
            }

        return zCommunication.progressWait(loginTask)
    }

    fun doLogout() = logoutFromDukascopy().runId(client)

    fun doBrokerTime(out_ServerTimeToFill: DoubleArray): Int
    {
        val brokerTimeResult = getServerTime().runId(environment)
        brokerTimeResult
            .maybeTime
            .map { out_ServerTimeToFill[0] = it }

        return brokerTimeResult.callResult
    }

    fun doSubscribeAsset(assetName: String): Int
    {
        val subscribeTask = subscribeAsset(assetName).runId(environment)
        return zCommunication.progressWait(subscribeTask)
    }

    fun doBrokerAsset(
        assetName: String,
        assetParams: DoubleArray
    ) = getAssetData(assetName, assetParams).runId(environment)

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
}