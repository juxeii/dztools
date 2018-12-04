package com.jforex.dzjforex.zorro

import arrow.data.*
import com.dukascopy.api.Instrument
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jforex.dzjforex.asset.getAssetParams
import com.jforex.dzjforex.login.getLoginTask
import com.jforex.dzjforex.misc.*
import com.jforex.dzjforex.settings.PluginSettings
import com.jforex.dzjforex.subscription.getSubscribeTask
import com.jforex.dzjforex.time.getConnectionState
import com.jforex.dzjforex.time.getServerTime
import com.jforex.dzjforex.time.toDATEFormatInSeconds
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
    private val pluginConfig = PluginConfig(
        client = client,
        pluginSettings = pluginSettings,
        natives = natives
    )
    private var quotes: Quotes = emptyMap()
    private lateinit var pluginConfigExt: PluginConfigExt

    fun doLogin(
        username: String,
        password: String,
        accountType: String,
        out_AccountNamesToFill: Array<String>
    ): Int {
        if (client.isConnected) return LOGIN_OK

        val loginResult = getLoginTask(
            username = username,
            password = password,
            accountType = accountType
        ).flatMap(::progressWait).runId(pluginConfig)

        if (loginResult == LOGIN_OK) {
            initStrategy()
            out_AccountNamesToFill[0] = getAccount { accountId }.runId(pluginConfigExt)
        }
        return loginResult
    }

    private fun initStrategy() {
        val infoStrategy = KForexUtilsStrategy()
        client.startStrategy(infoStrategy)
        logger.debug("started strategy")
        val kForexUtils = infoStrategy.kForexUtilsSingle().blockingFirst()
        kForexUtils
            .tickQuotes
            .subscribeBy(onNext = { quotes = saveQuote(it).runS(quotes) })

        pluginConfigExt = PluginConfigExt(
            pluginConfig = pluginConfig,
            infoStrategy = infoStrategy,
            kForexUtils = kForexUtils,
            quotes = quotes
        )
    }

    fun doLogout(): Int {
        client.disconnect()
        return LOGOUT_OK
    }

    fun doBrokerTime(out_ServerTimeToFill: DoubleArray) = if (!client.isConnected) CONNECTION_LOST_NEW_LOGIN_REQUIRED
    else getServerTime()
        .map { serverTime ->
            out_ServerTimeToFill[0] = toDATEFormatInSeconds(serverTime)
            serverTime
        }
        .flatMap(::getConnectionState)
        .runId(newConfigExt())

    fun doSubscribeAsset(assetName: String): Int {
        val subscribeTask = getSubscribeTask(assetName).runId(newConfigExt())
        return progressWait(subscribeTask).runId(pluginConfig)
    }

    fun doBrokerAsset(
        assetName: String,
        out_AssetParamsToFill: DoubleArray
    ) = instrumentFromAssetName(assetName)
        .map { getAssetParams(it).runId(newConfigExt()) }
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

    private fun newConfigExt() = pluginConfigExt.copy(quotes = quotes)
}

internal fun <T> progressWait(task: Single<T>) = ReaderApi
    .ask<PluginConfig>()
    .map { config ->
        val stateRelay = BehaviorRelay.create<T>()
        task
            .subscribeOn(Schedulers.io())
            .subscribe { it -> stateRelay.accept(it) }
        Observable.interval(
            0,
            config.pluginSettings.zorroProgressInterval(),
            TimeUnit.MILLISECONDS
        )
            .takeWhile { !stateRelay.hasValue() }
            .blockingSubscribe { config.natives.jcallback_BrokerProgress(heartBeatIndication) }

        stateRelay.value!!
    }
