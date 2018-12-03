package com.jforex.dzjforex.zorro

import arrow.data.Reader
import arrow.data.ReaderApi
import arrow.data.map
import arrow.data.runId
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jforex.dzjforex.asset.getAssetData
import com.jforex.dzjforex.login.loginToDukascopy
import com.jforex.dzjforex.login.logoutFromDukascopy
import com.jforex.dzjforex.misc.PluginEnvironment
import com.jforex.dzjforex.misc.PluginStrategy
import com.jforex.dzjforex.misc.Quotes
import com.jforex.dzjforex.misc.getClient
import com.jforex.dzjforex.settings.PluginSettings
import com.jforex.dzjforex.subscription.subscribeAsset
import com.jforex.dzjforex.time.getServerTime
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.aeonbits.owner.ConfigFactory
import org.apache.logging.log4j.LogManager
import java.util.concurrent.TimeUnit

private val logger = LogManager.getLogger()

class ZorroBridge
{
    private val client = getClient()
    private val natives = ZorroNatives()
    private val pluginSettings = ConfigFactory.create(PluginSettings::class.java)
    private val pluginStrategy = PluginStrategy(client, pluginSettings)
    private val environment = PluginEnvironment(
        client,
        pluginStrategy,
        pluginSettings,
        natives
    )

    fun doLogin(
        username: String,
        password: String,
        accountType: String,
        out_AccountNamesToFill: Array<String>
    ): Int
    {
        val loginResult = loginToDukascopy(
            username = username,
            password = password,
            accountType = accountType
        ).runId(environment)

        loginResult
            .maybeAccountName
            .map { out_AccountNamesToFill[0] = it }
        return loginResult.callResult
    }

    fun doLogout() = logoutFromDukascopy().runId(environment)

    fun doBrokerTime(out_ServerTimeToFill: DoubleArray) = getServerTime(out_ServerTimeToFill).runId(environment)

    fun doSubscribeAsset(assetName: String) = subscribeAsset(assetName).runId(environment)

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

    fun doSetOrderText(orderText: String): Int
    {
        return 42
    }
}

internal fun <T> progressWait(task: Single<T>): Reader<PluginEnvironment, T> = ReaderApi
    .ask<PluginEnvironment>()
    .map { env ->
        val stateRelay = BehaviorRelay.create<T>()
        task
            .subscribeOn(Schedulers.io())
            .subscribe { it -> stateRelay.accept(it) }
        Observable.interval(
            0,
            env.pluginSettings.zorroProgressInterval(),
            TimeUnit.MILLISECONDS
        )
            .takeWhile { !stateRelay.hasValue() }
            .blockingSubscribe { env.natives.jcallback_BrokerProgress(heartBeatIndication) }

        stateRelay.value!!
    }