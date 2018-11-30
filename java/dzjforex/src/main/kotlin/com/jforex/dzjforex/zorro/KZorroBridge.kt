package com.jforex.dzjforex.zorro

import arrow.data.runId
import arrow.instances.either.monad.map
import com.dukascopy.api.JFException
import com.jforex.dzjforex.Zorro
import com.jforex.dzjforex.asset.getAssetData
import com.jforex.dzjforex.login.LoginData
import com.jforex.dzjforex.login.loginToDukascopy
import com.jforex.dzjforex.login.logoutFromDukascopy
import com.jforex.dzjforex.misc.PluginEnvironment
import com.jforex.dzjforex.misc.PluginStrategy
import com.jforex.dzjforex.misc.getClient
import com.jforex.dzjforex.misc.waitForFirstQuote
import com.jforex.dzjforex.settings.PluginSettings
import com.jforex.dzjforex.subscription.subscribeAsset
import com.jforex.dzjforex.time.getServerTime
import io.reactivex.Observable
import org.aeonbits.owner.ConfigFactory
import org.apache.logging.log4j.LogManager

class KZorroBridge {
    private val client = getClient()
    private val zoro = Zorro()
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
    ): Int {
        val loginData = LoginData(
            username,
            password,
            accountType
        )
        val loginTask = loginToDukascopy(loginData)
            .runId(client)
            .map { loginResult ->
                if (loginResult == LOGIN_OK) {
                    pluginStrategy.start(out_AccountNames)
                }
                loginResult
            }

        return zCommunication.progressWait(loginTask)
    }

    fun doLogout(): Int {
        logger.debug("Logout called")
        pluginStrategy.stop()
        return logoutFromDukascopy()
            .runId(client)
            .blockingGet()
    }

    fun doBrokerTime(out_ServerTimeToFill: DoubleArray): Int {
        val brokerTimeResult = getServerTime().runId(environment)
        brokerTimeResult
            .maybeTime
            .map { out_ServerTimeToFill[0] = it }

        return brokerTimeResult.callResult
    }

    fun doSubscribeAsset(assetName: String): Int {
        if (subscribeAsset(assetName).runId(environment) == SUBSCRIBE_FAIL) return SUBSCRIBE_FAIL

        val waitForQuoteTask = Observable
            .fromIterable(client.subscribedInstruments)
            .map { instrument ->
                waitForFirstQuote(instrument)
                    .run(environment)
                    .map { }
                    .fold({ throw JFException("No quote for $instrument available!") }, { instrument })
            }
            .ignoreElements()
            .toSingleDefault(SUBSCRIBE_OK)
            .onErrorReturnItem(SUBSCRIBE_FAIL)

        return zCommunication.progressWait(waitForQuoteTask)
    }

    fun doBrokerAsset(
        assetName: String,
        assetParams: DoubleArray
    ) = getAssetData(assetName, assetParams).runId(environment)

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