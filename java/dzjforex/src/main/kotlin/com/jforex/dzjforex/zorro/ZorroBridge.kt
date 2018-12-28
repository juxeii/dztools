package com.jforex.dzjforex.zorro

import com.jforex.dzjforex.account.BrokerAccountApi.brokerAccount
import com.jforex.dzjforex.account.BrokerAccountSuccess
import com.jforex.dzjforex.asset.BrokerAssetApi.brokerAsset
import com.jforex.dzjforex.asset.createBrokerAssetApi
import com.jforex.dzjforex.buy.BrokerBuyApi.brokerBuy
import com.jforex.dzjforex.buy.createBrokerBuyApi
import com.jforex.dzjforex.command.commandbyId
import com.jforex.dzjforex.history.BrokerHistoryApi.brokerHistory
import com.jforex.dzjforex.login.LoginApi.create
import com.jforex.dzjforex.login.LoginApi.logout
import com.jforex.dzjforex.login.loginApi
import com.jforex.dzjforex.misc.PluginApi.isConnected
import com.jforex.dzjforex.misc.contextApi
import com.jforex.dzjforex.misc.pluginApi
import com.jforex.dzjforex.misc.runDirect
import com.jforex.dzjforex.misc.runWithProgress
import com.jforex.dzjforex.sell.BrokerSellApi.brokerSell
import com.jforex.dzjforex.stop.BrokerStopApi.brokerStop
import com.jforex.dzjforex.subscription.BrokerSubscribeApi.subscribeInstrument
import com.jforex.dzjforex.subscription.createBrokerSubscribeApi
import com.jforex.dzjforex.time.BrokerTimeApi.brokerTime
import com.jforex.dzjforex.time.BrokerTimeSuccess
import com.jforex.dzjforex.trade.BrokerTradeApi.brokerTrade
import com.jforex.dzjforex.trade.createBrokerTradeApi

class ZorroBridge
{
    fun doLogin(username: String, password: String, accountType: String, out_AccountNamesToFill: Array<String>) =
        runWithProgress(loginApi.create(username, password, accountType, out_AccountNamesToFill))

    fun doLogout() = runDirect(loginApi.logout())

    fun doBrokerTime(out_ServerTimeToFill: DoubleArray): Int
    {
        val brokerTimeResult = runDirect(contextApi.brokerTime())
        if (brokerTimeResult is BrokerTimeSuccess)
        {
            val iTimeUTC = 0
            out_ServerTimeToFill[iTimeUTC] = brokerTimeResult.serverTime
        }
        return brokerTimeResult.returnCode
    }

    fun doSubscribeAsset(assetName: String) =
        runWithProgress(createBrokerSubscribeApi().run { subscribeInstrument(assetName) })

    fun doBrokerAsset(assetName: String, out_AssetParamsToFill: DoubleArray) =
        runDirect(createBrokerAssetApi().brokerAsset(assetName, out_AssetParamsToFill))

    fun doBrokerAccount(out_AccountInfoToFill: DoubleArray): Int
    {
        val brokerAccountResult = runDirect(contextApi.brokerAccount())
        if (brokerAccountResult is BrokerAccountSuccess)
        {
            val iBalance = 0
            val iTradeVal = 1
            val iMarginVal = 2
            with(brokerAccountResult.data) {
                out_AccountInfoToFill[iBalance] = balance
                out_AccountInfoToFill[iTradeVal] = tradeVal
                out_AccountInfoToFill[iMarginVal] = marginVal
            }
        }
        return brokerAccountResult.returnCode
    }

    fun doBrokerTrade(orderId: Int, out_TradeInfoToFill: DoubleArray) =
        runDirect(createBrokerTradeApi().brokerTrade(orderId, out_TradeInfoToFill))

    fun doBrokerBuy2(
        assetName: String,
        contracts: Int,
        slDistance: Double,
        limit: Double,
        out_TradeInfoToFill: DoubleArray
    ) = runWithProgress(
        createBrokerBuyApi().brokerBuy(
            assetName,
            contracts,
            slDistance,
            limit,
            out_TradeInfoToFill
        )
    )

    fun doBrokerSell(orderId: Int, contracts: Int) = runWithProgress(contextApi.brokerSell(orderId, contracts))

    fun doBrokerStop(orderId: Int, slPrice: Double) = runWithProgress(contextApi.brokerStop(orderId, slPrice))

    fun doBrokerHistory2(
        assetName: String,
        utcStartDate: Double,
        utcEndDate: Double,
        periodInMinutes: Int,
        noOfTicks: Int,
        out_TickInfoToFill: DoubleArray
    ) = runWithProgress(
        contextApi.brokerHistory(
            assetName,
            utcStartDate,
            utcEndDate,
            periodInMinutes,
            noOfTicks,
            out_TickInfoToFill
        )
    )

    fun doBrokerCommand(commandId: Int, bytes: ByteArray) =
        if (!pluginApi.isConnected()) BROKER_COMMAND_UNAVAILABLE
        else commandbyId.getOrDefault(commandId) { _ -> BROKER_COMMAND_UNAVAILABLE }(bytes)
}
