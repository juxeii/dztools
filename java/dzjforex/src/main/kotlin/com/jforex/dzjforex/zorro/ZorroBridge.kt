package com.jforex.dzjforex.zorro

import arrow.data.runId
import arrow.data.runS
import com.dukascopy.api.Instrument
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jforex.dzjforex.asset.getAssetParams
import com.jforex.dzjforex.misc.*
import com.jforex.dzjforex.settings.PluginSettings
import com.jforex.dzjforex.subscription.getSubscribeTask
import com.jforex.dzjforex.time.getBrokerTimeResult
import com.jforex.kforexutils.authentification.LoginCredentials
import com.jforex.kforexutils.authentification.LoginType
import com.jforex.kforexutils.client.login
import com.jforex.kforexutils.misc.KForexUtils
import com.jforex.kforexutils.price.TickQuote
import com.jforex.kforexutils.strategy.KForexUtilsStrategy
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.aeonbits.owner.ConfigFactory
import org.apache.logging.log4j.LogManager
import java.util.concurrent.TimeUnit

private val logger = LogManager.getLogger()

typealias Quotes = Map<Instrument, TickQuote>

class ZorroBridge {
    private val client = getClient()
    private val natives = ZorroNatives()
    private val pluginSettings = ConfigFactory.create(PluginSettings::class.java)
    private val infoStrategy = KForexUtilsStrategy()
    private var quotes: Quotes = emptyMap()
    private lateinit var kForexUtils: KForexUtils
    private lateinit var pluginConfig: PluginConfig

    fun doLogin(
        username: String,
        password: String,
        accountType: String,
        out_AccountNamesToFill: Array<String>
    ): Int {
        if (client.isConnected) return LOGIN_OK

        val loginCredentials = LoginCredentials(username = username, password = password)
        val loginType = if (accountType == realLoginType) LoginType.LIVE
        else LoginType.DEMO

        val loginTask = client
            .login(loginCredentials, loginType)
            .toSingle {
                initStrategy()
                out_AccountNamesToFill[0] = getAccount { accountId }.runId(pluginConfig)
                LOGIN_OK
            }
            .doOnError { logger.debug("Login failed! " + it.message) }
            .onErrorReturnItem(LOGIN_FAIL)

        return progressWait(loginTask)
    }

    private fun initStrategy() {
        client.startStrategy(infoStrategy)
        logger.debug("started strategy")
        val kForexUtils = infoStrategy.kForexUtilsSingle().blockingFirst()
        kForexUtils
            .tickQuotes
            .subscribeBy(onNext = {
                logger.debug("Quotes before $quotes")
                quotes = saveQuote(it).runS(quotes)
                logger.debug("Quotes after $quotes")
                renewExtConfig()
            })

        renewExtConfig()
    }

    private fun renewExtConfig() {
        pluginConfig = PluginConfig(
            client = client,
            pluginSettings = pluginSettings,
            natives = natives,
            infoStrategy = infoStrategy,
            kForexUtils = kForexUtils,
            quotes = quotes
        )
    }

    fun doLogout(): Int {
        client.disconnect()
        return LOGOUT_OK
    }

    fun doBrokerTime(out_ServerTimeToFill: DoubleArray): Int {
        val brokerTimeResult = getBrokerTimeResult().runId(pluginConfig)
        brokerTimeResult
            .maybeServerTime
            .map { out_ServerTimeToFill[0] = it }

        return brokerTimeResult.connectionState
    }

    fun doSubscribeAsset(assetName: String): Int {
        val subscribeTask = getSubscribeTask(assetName).runId(pluginConfig)
        return progressWait(subscribeTask)
    }

    fun doBrokerAsset(
        assetName: String,
        out_AssetParamsToFill: DoubleArray
    ) = instrumentFromAssetName(assetName)
        .map { getAssetParams(it).runId(pluginConfig) }
        .fold({ ASSET_UNAVAILABLE }) {
            out_AssetParamsToFill[0] = it.price
            out_AssetParamsToFill[1] = it.spread
            ASSET_AVAILABLE
        }

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

    fun doSetOrderText(orderText: String): Int {
        return 42
    }

    internal fun <T> progressWait(task: Single<T>): T {
        val stateRelay = BehaviorRelay.create<T>()
        task
            .subscribeOn(Schedulers.io())
            .subscribe { it -> stateRelay.accept(it) }
        Observable.interval(
            0,
            pluginSettings.zorroProgressInterval(),
            TimeUnit.MILLISECONDS
        )
            .takeWhile { !stateRelay.hasValue() }
            .blockingSubscribe { natives.jcallback_BrokerProgress(heartBeatIndication) }

       return stateRelay.value!!
    }
}
