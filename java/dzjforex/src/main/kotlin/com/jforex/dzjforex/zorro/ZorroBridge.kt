package com.jforex.dzjforex.zorro

import arrow.effects.IO
import arrow.effects.instances.io.monad.monad
import com.jforex.dzjforex.account.BrokerAccountApi.brokerAccount
import com.jforex.dzjforex.asset.BrokerAssetApi.brokerAsset
import com.jforex.dzjforex.buy.BrokerBuyApi.brokerBuy
import com.jforex.dzjforex.command.BrokerCommandApi.getAccount
import com.jforex.dzjforex.command.BrokerCommandApi.getDigits
import com.jforex.dzjforex.command.BrokerCommandApi.getMarginInit
import com.jforex.dzjforex.command.BrokerCommandApi.getMaxLot
import com.jforex.dzjforex.command.BrokerCommandApi.getMaxTicks
import com.jforex.dzjforex.command.BrokerCommandApi.getMinLot
import com.jforex.dzjforex.command.BrokerCommandApi.getServerState
import com.jforex.dzjforex.command.BrokerCommandApi.getTime
import com.jforex.dzjforex.command.BrokerCommandApi.getTradeAllowed
import com.jforex.dzjforex.command.BrokerCommandApi.setLimit
import com.jforex.dzjforex.command.BrokerCommandApi.setOrderText
import com.jforex.dzjforex.command.BrokerCommandApi.setSlippage
import com.jforex.dzjforex.command.getBcOrderText
import com.jforex.dzjforex.command.getBcSlippage
import com.jforex.dzjforex.command.maybeBcLimitPrice
import com.jforex.dzjforex.history.BrokerHistoryApi.brokerHistory
import com.jforex.dzjforex.login.LoginApi.brokerLogin
import com.jforex.dzjforex.login.LoginApi.logout
import com.jforex.dzjforex.misc.contextApi
import com.jforex.dzjforex.misc.pluginApi
import com.jforex.dzjforex.misc.runDirect
import com.jforex.dzjforex.misc.runWithProgress
import com.jforex.dzjforex.sell.BrokerSellApi.brokerSell
import com.jforex.dzjforex.stop.BrokerStopApi.brokerStop
import com.jforex.dzjforex.subscribe.BrokerSubscribeApi.brokerSubscribe
import com.jforex.dzjforex.time.BrokerTimeApi.brokerTime
import com.jforex.dzjforex.trade.BrokerTradeApi.brokerTrade

@Suppress("unused")
class ZorroBridge
{
    fun brokerLogin(username: String, password: String, accountType: String) =
        runWithProgress(
            pluginApi.brokerLogin(
                username = username,
                password = password,
                accountType = accountType
            )
        )

    fun brokerLogout() = runDirect(pluginApi.logout())

    fun brokerTime() = runDirect(contextApi.brokerTime())

    fun brokerSubscribeAsset(assetName: String) = runWithProgress(contextApi.brokerSubscribe(assetName))

    fun brokerAsset(assetName: String) = runDirect(contextApi.brokerAsset(assetName))

    fun brokerAccount() = runDirect(contextApi.brokerAccount())

    fun brokerTrade(orderId: Int) = runDirect(contextApi.brokerTrade(orderId))

    fun brokerBuy2(
        assetName: String,
        contracts: Int,
        slDistance: Double,
        limitPrice: Double
    ) = runWithProgress(
        contextApi.brokerBuy(
            assetName = assetName,
            contracts = contracts,
            slDistance = slDistance,
            limitPrice = limitPrice,
            slippage = getBcSlippage(),
            orderText = getBcOrderText()
        )
    )

    fun brokerSell(orderId: Int, contracts: Int) =
        runWithProgress(contextApi.brokerSell(orderId, contracts, maybeBcLimitPrice(), getBcSlippage()))

    fun brokerStop(orderId: Int, slPrice: Double) = runWithProgress(contextApi.brokerStop(orderId, slPrice))

    fun brokerHistory2(
        assetName: String,
        utcStartDate: Double,
        utcEndDate: Double,
        periodInMinutes: Int,
        noOfTicks: Int
    ) = runWithProgress(
        contextApi.brokerHistory(
            assetName = assetName,
            utcStartDate = utcStartDate,
            utcEndDate = utcEndDate,
            periodInMinutes = periodInMinutes,
            noOfTicks = noOfTicks
        )
    )

    fun bcSetOrderText(orderText: String) = runDirect(contextApi.setOrderText(orderText))

    fun bcSetSlippage(slippage: Double) = runDirect(contextApi.setSlippage(slippage))

    fun bcSetLimit(limit: Double) = runDirect(contextApi.setLimit(limit))

    fun bcGetAccount() = runDirect(contextApi.getAccount())

    fun bcGetDigits(assetName: String) = runDirect(contextApi.getDigits(assetName))

    fun bcGetMaxLot(assetName: String) = runDirect(contextApi.getMaxLot(assetName))

    fun bcGetMinLot(assetName: String) = runDirect(contextApi.getMinLot(assetName))

    fun bcGetMarginInit(assetName: String) = runDirect(contextApi.getMarginInit(assetName))

    fun bcGetTradeAllowed(assetName: String) = runDirect(contextApi.getTradeAllowed(assetName))

    fun bcGetTime() = runDirect(contextApi.getTime())

    fun bcGetMaxTicks() = runDirect(contextApi.getMaxTicks())

    fun bcGetServerState() = runDirect(contextApi.getServerState())
}
