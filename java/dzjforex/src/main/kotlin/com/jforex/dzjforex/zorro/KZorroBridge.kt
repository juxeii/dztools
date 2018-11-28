package com.jforex.dzjforex.zorro

import arrow.data.runId
import com.jforex.dzjforex.Zorro
import com.jforex.dzjforex.asset.BrokerAsset
import com.jforex.dzjforex.login.BrokerLogin
import com.jforex.dzjforex.login.LoginData
import com.jforex.dzjforex.misc.PluginEnvironment
import com.jforex.dzjforex.misc.PluginStrategy
import com.jforex.dzjforex.misc.getClient
import com.jforex.dzjforex.settings.PluginSettings
import com.jforex.dzjforex.subscription.subscribeAsset
import com.jforex.dzjforex.time.getServerTime
import org.aeonbits.owner.ConfigFactory
import org.apache.logging.log4j.LogManager

class KZorroBridge {
    private val client = getClient()
    private val zoro = Zorro()
    private val brokerLogin = BrokerLogin(client)
    private val pluginSettings = ConfigFactory.create(PluginSettings::class.java)
    private val zCommunication = ZorroCommunication(pluginSettings)
    private val pluginStrategy = PluginStrategy(client, pluginSettings)
    private val environment = PluginEnvironment(client, pluginStrategy)
    private lateinit var brokerAsset: BrokerAsset

    private val logger = LogManager.getLogger(KZorroBridge::class.java)

    private fun initComponents() {
        logger.debug("Init components")
        brokerAsset = BrokerAsset(pluginStrategy.quoteProvider)
        logger.debug("Init components done")
    }

    fun doLogin(
        username: String,
        password: String,
        accountType: String,
        out_AccountNames: Array<String>
    ): Int {
        logger.debug("Login called")
        val loginData = LoginData(
            username,
            password,
            accountType
        )
        val loginTask = brokerLogin
            .login(loginData)
            .map { loginResult ->
                logger.debug("Login result $loginResult")
                if (loginResult == LOGIN_OK) {
                    pluginStrategy.start(out_AccountNames)
                    initComponents()
                }
                loginResult
            }

        return zCommunication.progressWait(loginTask)
    }

    fun doLogout(): Int {
        logger.debug("Logout called")
        pluginStrategy.stop()
        return brokerLogin
            .logout()
            .blockingGet()
    }

    fun doBrokerTime(out_ServerTimeToFill: DoubleArray): Int {
        val brokerTimeResult = getServerTime().runId(environment)
        brokerTimeResult
            .maybeTime
            .map { out_ServerTimeToFill[0] = it }

        return brokerTimeResult.callResult
    }

    fun doSubscribeAsset(assetName: String) = subscribeAsset(assetName).runId(environment)

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