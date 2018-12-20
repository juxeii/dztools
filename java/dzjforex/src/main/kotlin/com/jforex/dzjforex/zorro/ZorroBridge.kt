package com.jforex.dzjforex.zorro

import arrow.core.ForTry
import arrow.core.Try
import arrow.core.fix
import arrow.data.runS
import arrow.effects.DeferredK
import arrow.effects.IO
import arrow.effects.fix
import arrow.effects.instances.io.applicativeError.handleError
import arrow.effects.instances.io.monad.flatMap
import arrow.effects.instances.io.monadDefer.monadDefer
import arrow.effects.instances.io.monadError.monadError
import arrow.instances.`try`.monadError.monadError
import com.dukascopy.api.Instrument
import com.jforex.dzjforex.account.AccountDependencies
import com.jforex.dzjforex.asset.BrokerAssetApi.getAssetParams
import com.jforex.dzjforex.asset.BrokerAssetDependencies
import com.jforex.dzjforex.history.HistoryDependencies
import com.jforex.dzjforex.init.StrategyInitApi.start
import com.jforex.dzjforex.init.StrategyInitDependencies
import com.jforex.dzjforex.init.account
import com.jforex.dzjforex.init.context
import com.jforex.dzjforex.init.quotes
import com.jforex.dzjforex.login.LoginApi.login
import com.jforex.dzjforex.login.LoginDependencies
import com.jforex.dzjforex.misc.*
import com.jforex.dzjforex.misc.ProgressWaitApi.wait
import com.jforex.dzjforex.settings.PluginSettings
import com.jforex.dzjforex.settings.SettingsDependencies
import com.jforex.dzjforex.subscription.BrokerSubscribeApi.subscribeInstrument
import com.jforex.dzjforex.subscription.BrokerSubscribeApi.waitForLatestQuotes
import com.jforex.dzjforex.subscription.BrokerSubscribeDependencies
import com.jforex.dzjforex.time.BrokerTimeApi.getBrokerTimeResult
import com.jforex.dzjforex.time.BrokerTimeDependencies
import com.jforex.kforexutils.price.TickQuote
import com.jforex.kforexutils.strategy.KForexUtilsStrategy
import org.aeonbits.owner.ConfigFactory
import org.apache.logging.log4j.LogManager

private val logger = LogManager.getLogger()

fun saveQuote(quote: TickQuote)
{
    quotes = updateQuotes(quote).runS(quotes)
}

class ZorroBridge
{
    private val natives = ZorroNatives()
    private val client = getClient()
    private val pluginSettings = ConfigFactory.create(PluginSettings::class.java)
    private val settingsApi = SettingsDependencies(pluginSettings)
    private val progressWaitApi = ProgressWaitDependencies(pluginSettings, natives)
    private val loginApi = LoginDependencies(IO.monadDefer(), client)
    private val strategyInitApi = StrategyInitDependencies(IO.monadError(), client, KForexUtilsStrategy())
    private lateinit var contextApi: ContextDependencies
    private lateinit var accountApi: AccountDependencies
    private lateinit var historyApi: HistoryDependencies<ForTry>
    private lateinit var brokerTimeApi: BrokerTimeDependencies<ForTry>

    private fun createQuoteProviderApi() = QuoteProviderDependencies(quotes)

    private fun createBrokerSubscribeApi(): BrokerSubscribeDependencies<ForTry> =
        BrokerSubscribeDependencies(historyApi, contextApi, accountApi, createQuoteProviderApi())

    private fun createBrokerAssetApi(instrument: Instrument): BrokerAssetDependencies =
        BrokerAssetDependencies(instrument, createQuoteProviderApi())

    fun doLogin(
        username: String,
        password: String,
        accountType: String,
        out_AccountNamesToFill: Array<String>
    ): Int
    {
        if (client.isConnected) return LOGIN_OK

        val loginTask = DeferredK {
            loginApi
                .login(username, password, accountType)
                .flatMap { strategyInitApi.start() }
                .map {
                    out_AccountNamesToFill[0] = account.accountId
                    contextApi = ContextDependencies(context)
                    accountApi = AccountDependencies(account, settingsApi)
                    historyApi = HistoryDependencies(context.history, settingsApi, Try.monadError())
                    brokerTimeApi = BrokerTimeDependencies(Try.monadError(), accountApi, context)
                    LOGIN_OK
                }
                .handleError {
                    logger.debug("Login failed! $it")
                    LOGIN_FAIL
                }
                .fix()
                .unsafeRunSync()
        }
        return progressWaitApi.wait(loginTask)
    }

    fun doLogout(): Int
    {
        client.disconnect()
        return LOGOUT_OK
    }

    fun doBrokerTime(out_ServerTimeToFill: DoubleArray): Int
    {
        if (!client.isConnected) return CONNECTION_LOST_NEW_LOGIN_REQUIRED

        brokerTimeApi
            .getBrokerTimeResult()
            .fix()
            .fold({
                logger.debug("Error fetching broker time! $it")
                return CONNECTION_OK_BUT_MARKET_CLOSED
            }) { brokerTimeResult ->
                out_ServerTimeToFill[0] = brokerTimeResult.serverTime
                return brokerTimeResult.connectionState
            }
    }

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
        return progressWaitApi.wait(subscribeTask)
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
