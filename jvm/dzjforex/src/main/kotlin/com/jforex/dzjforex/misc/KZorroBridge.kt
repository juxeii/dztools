package com.jforex.dzjforex.misc

import com.jforex.dzjforex.login.BrokerLogin
import com.jforex.dzjforex.login.LoginData
import com.jforex.dzjforex.settings.PluginSettings
import org.aeonbits.owner.ConfigFactory
import org.apache.logging.log4j.LogManager

class KZorroBridge
{
    private val client = getClient()
    private val brokerLogin = BrokerLogin(client)
    private val pluginSettings = ConfigFactory.create(PluginSettings::class.java)
    private val zCommunication = ZorroCommunication(pluginSettings)
    private val pluginStrategy = PluginStrategy(client, pluginSettings)

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
        val loginTask = brokerLogin
            .login(loginData)
            .doOnSuccess { pluginStrategy.start(out_AccountNames) }

        return zCommunication.progressWait(loginTask)
    }

    fun doLogout(): Int
    {
        pluginStrategy.stop()
        return brokerLogin
            .logout()
            .blockingGet()
    }

    fun doBrokerTime(pTimeUTC: DoubleArray): Int
    {
        return 42
    }

    fun doSubscribeAsset(assetName: String): Int
    {
        return 42
    }

    fun doBrokerAsset(
        assetName: String,
        assetParams: DoubleArray
    ): Int
    {
        return 42
    }

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