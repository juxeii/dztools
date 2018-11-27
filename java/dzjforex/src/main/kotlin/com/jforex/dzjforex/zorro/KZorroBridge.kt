package com.jforex.dzjforex.zorro

import com.jforex.dzjforex.asset.BrokerAsset
import com.jforex.dzjforex.login.BrokerLogin
import com.jforex.dzjforex.login.LoginData
import com.jforex.dzjforex.misc.PluginStrategy
import com.jforex.dzjforex.misc.getClient
import com.jforex.dzjforex.settings.PluginSettings
import com.jforex.dzjforex.subscription.BrokerSubscribe
import com.jforex.dzjforex.time.BrokerTime
import org.aeonbits.owner.ConfigFactory
import org.apache.logging.log4j.LogManager

class KZorroBridge {
    private val client = getClient()
    private val brokerLogin = BrokerLogin(client)
    private val pluginSettings = ConfigFactory.create(PluginSettings::class.java)
    private val zCommunication = ZorroCommunication(pluginSettings)
    private val pluginStrategy = PluginStrategy(client, pluginSettings)
    private lateinit var brokerSubscribe: BrokerSubscribe
    private lateinit var brokerTime: BrokerTime
    private lateinit var brokerAsset: BrokerAsset

    private val logger = LogManager.getLogger(KZorroBridge::class.java)

    private fun initComponents() {
        brokerSubscribe = BrokerSubscribe(client, pluginStrategy.accountInfo)
        brokerTime = BrokerTime(
            client,
            pluginStrategy.context,
            pluginStrategy.accountInfo
        )
        brokerAsset = BrokerAsset()
    }

    fun doLogin(
        username: String,
        password: String,
        accountType: String,
        out_AccountNames: Array<String>
    ): Int {
        val loginData = LoginData(
            username,
            password,
            accountType
        )
        val loginTask = brokerLogin
            .login(loginData)
            .map { loginResult ->
                if (loginResult == LOGIN_OK) {
                    pluginStrategy.start(out_AccountNames)
                    initComponents()
                }
                loginResult
            }

        return zCommunication.progressWait(loginTask)
    }

    fun doLogout(): Int {
        pluginStrategy.stop()
        return brokerLogin
            .logout()
            .blockingGet()
    }

    fun doBrokerTime(pTimeUTC: DoubleArray) = brokerTime.get(pTimeUTC)

    fun doSubscribeAsset(assetName: String) = brokerSubscribe.subscribe(assetName)

    fun doBrokerAsset(
        assetName: String,
        assetParams: DoubleArray
    ) = brokerAsset.get(assetName, assetParams)

    fun doBrokerAccount(accountInfoParams: DoubleArray): Int {
        return 42
    }

    fun doBrokerTrade(
        orderID: Int,
        tradeParams: DoubleArray
    ): Int {
        return 42
    }

    fun doBrokerBuy2(
        assetName: String,
        contracts: Int,
        slDistance: Double,
        limit: Double,
        tradeParams: DoubleArray
    ): Int {
        return 42
    }

    fun doBrokerSell(
        orderID: Int,
        contracts: Int
    ): Int {
        return 42
    }

    fun doBrokerStop(
        orderID: Int,
        slPrice: Double
    ): Int {
        return 42
    }

    fun doBrokerHistory2(
        assetName: String,
        utcStartDate: Double,
        utcEndDate: Double,
        periodInMinutes: Int,
        noOfTicks: Int,
        tickParams: DoubleArray
    ): Int {
        return 42
    }
}