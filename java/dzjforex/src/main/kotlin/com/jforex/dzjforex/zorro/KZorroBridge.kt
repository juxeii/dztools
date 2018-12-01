package com.jforex.dzjforex.zorro

import arrow.data.runId
import com.jforex.dzjforex.asset.getAssetData
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
    private val pluginStrategy = PluginStrategy(client, pluginSettings)
    private val environment = PluginEnvironment(client, pluginStrategy, pluginSettings)

    private val logger = LogManager.getLogger(KZorroBridge::class.java)

    fun login(
        username: String,
        password: String,
        accountType: String,
        out_AccountNamesToFill: Array<String>
    ) = loginToDukascopy(
        username = username,
        password = password,
        accountType = accountType,
        out_AccountNamesToFill = out_AccountNamesToFill
    ).runId(environment)

    fun logout() = logoutFromDukascopy().runId(environment)

    fun brokerTime(out_ServerTimeToFill: DoubleArray) = getServerTime(out_ServerTimeToFill).runId(environment)

    fun subscribe(assetName: String): Int
    {
        val subscribeTask = subscribeAsset(assetName).runId(environment)
        return progressWait(subscribeTask).runId(environment)
    }

    fun brokerAsset(
        assetName: String,
        assetParams: DoubleArray
    ) = getAssetData(assetName, assetParams).runId(environment)

    fun brokerAccount(accountInfoParams: DoubleArray): Int
    {
        return 42
    }

    fun brokerTrade(
        orderID: Int,
        tradeParams: DoubleArray
    ): Int
    {
        return 42
    }

    fun brokerBuy(
        assetName: String,
        contracts: Int,
        slDistance: Double,
        limit: Double,
        tradeParams: DoubleArray
    ): Int
    {
        return 42
    }

    fun brokerSell(
        orderID: Int,
        contracts: Int
    ): Int
    {
        return 42
    }

    fun brokerStop(
        orderID: Int,
        slPrice: Double
    ): Int
    {
        return 42
    }

    fun brokerHistory(
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