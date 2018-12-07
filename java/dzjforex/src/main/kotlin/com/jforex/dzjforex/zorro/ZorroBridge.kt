package com.jforex.dzjforex.zorro

import arrow.core.Eval
import arrow.core.ForTry
import arrow.core.Try
import arrow.core.fix
import arrow.data.ReaderT
import arrow.data.fix
import arrow.data.runId
import arrow.data.runS
import arrow.instances.`try`.monad.monad
import arrow.instances.kleisli.monad.monad
import arrow.typeclasses.binding
import com.dukascopy.api.Instrument
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jforex.dzjforex.asset.getAssetParams
import com.jforex.dzjforex.login.login
import com.jforex.dzjforex.misc.*
import com.jforex.dzjforex.settings.PluginSettings
import com.jforex.dzjforex.subscription.getInitialQuotes
import com.jforex.dzjforex.subscription.subscribeInstrument
import com.jforex.dzjforex.time.getBrokerTimeResult
import com.jforex.kforexutils.misc.KForexUtils
import com.jforex.kforexutils.price.TickQuote
import com.jforex.kforexutils.strategy.KForexUtilsStrategy
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.aeonbits.owner.ConfigFactory
import org.apache.logging.log4j.LogManager

private val logger = LogManager.getLogger()

typealias Quotes = Map<Instrument, TickQuote>

class ZorroBridge
{
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
    ): Int
    {
        if (client.isConnected) return LOGIN_OK
        val loginTask = Eval.later {
            login(
                username = username,
                password = password,
                accountType = accountType
            )
                .runId(client)
                .fold({ LOGIN_FAIL }) {
                    initStrategy()
                    out_AccountNamesToFill[0] = getAccount { accountId }.runId(pluginConfig)
                    LOGIN_OK
                }
        }
        return progressWait(loginTask)
    }

    private fun initStrategy()
    {
        client.startStrategy(infoStrategy)
        logger.debug("started strategy")
        kForexUtils = infoStrategy.kForexUtilsSingle().blockingFirst()
        kForexUtils
            .tickQuotes
            .subscribeBy(onNext = {
                quotes = saveQuote(it).runS(quotes)
                renewExtConfig()
            })

        renewExtConfig()
    }

    private fun renewExtConfig()
    {
        pluginConfig = PluginConfig(
            client = client,
            pluginSettings = pluginSettings,
            natives = natives,
            infoStrategy = infoStrategy,
            kForexUtils = kForexUtils,
            quotes = quotes
        )
    }

    fun doLogout(): Int
    {
        client.disconnect()
        return LOGOUT_OK
    }

    fun doBrokerTime(out_ServerTimeToFill: DoubleArray): Int
    {
        val brokerTimeResult = getBrokerTimeResult().runId(pluginConfig)
        brokerTimeResult
            .maybeServerTime
            .map { out_ServerTimeToFill[0] = it }

        return brokerTimeResult.connectionState
    }

    fun doSubscribeAsset(assetName: String): Int
    {
        val subscribeTask = Eval.later {
            ReaderT.monad<ForTry, PluginConfig>(Try.monad()).binding {
                val subscribedInstruments = subscribeInstrument(assetName).bind()
                natives.showInZorroWindow("Waiting for initial quotes of $subscribedInstruments")
                val initialQuotes = getInitialQuotes(subscribedInstruments).bind()
                initialQuotes.forEach { infoStrategy.onTick(it.instrument, it.tick) }
            }
                .fix()
                .run(pluginConfig)
                .fix()
                .fold({ SUBSCRIBE_FAIL }) { SUBSCRIBE_OK }
        }
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

    private fun <T> progressWait(task: Eval<T>): T
    {
        val stateRelay = BehaviorRelay.create<T>()
        GlobalScope.launch { stateRelay.accept(task.value()) }
        runBlocking {
            while (!stateRelay.hasValue())
            {
                natives.jcallback_BrokerProgress(heartBeatIndication)
                delay(pluginSettings.zorroProgressInterval())
            }
        }
        return stateRelay.value!!
    }
}
