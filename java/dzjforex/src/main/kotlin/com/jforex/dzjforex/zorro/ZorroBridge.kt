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

class ZorroBridge
{
    fun doLogin(username: String, password: String, accountType: String, out_AccountNamesToFill: Array<String>) =
        runWithProgress(
            pluginApi.brokerLogin(
                username = username,
                password = password,
                accountType = accountType,
                out_AccountNamesToFill = out_AccountNamesToFill
            )
        )

    fun doLogout() = runDirect(pluginApi.logout())

    fun doBrokerTime(out_ServerTimeToFill: DoubleArray) = runDirect(contextApi.brokerTime(out_ServerTimeToFill))

    fun doSubscribeAsset(assetName: String) = runWithProgress(contextApi.brokerSubscribe(assetName))

    fun doBrokerAsset(assetName: String, out_AssetParamsToFill: DoubleArray) =
        runDirect(contextApi.brokerAsset(assetName, out_AssetParamsToFill))

    fun doBrokerAccount(out_AccountInfoToFill: DoubleArray): Int =
        runDirect(contextApi.brokerAccount(out_AccountInfoToFill))

    fun doBrokerTrade(orderId: Int, out_TradeInfoToFill: DoubleArray) =
        runDirect(contextApi.brokerTrade(orderId, out_TradeInfoToFill))

    fun doBrokerBuy2(
        assetName: String,
        contracts: Int,
        slDistance: Double,
        limitPrice: Double,
        out_BuyInfoToFill: DoubleArray
    ) = runWithProgress(
        contextApi.brokerBuy(
            assetName = assetName,
            contracts = contracts,
            slDistance = slDistance,
            limitPrice = limitPrice,
            slippage = getBcSlippage(),
            orderText = getBcOrderText(),
            out_BuyInfoToFill = out_BuyInfoToFill
        )
    )

    fun doBrokerSell(orderId: Int, contracts: Int) =
        runWithProgress(contextApi.brokerSell(orderId, contracts, maybeBcLimitPrice(), getBcSlippage()))

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
            assetName = assetName,
            utcStartDate = utcStartDate,
            utcEndDate = utcEndDate,
            periodInMinutes = periodInMinutes,
            noOfTicks = noOfTicks,
            out_TickInfoToFill = out_TickInfoToFill
        )
    )

    fun doBrokerCommand(commandId: Int, bytes: ByteArray, out_CommandResultToFill: DoubleArray)
    {
        val commandCall = with(contextApi) {
            when (commandId)
            {
                SET_ORDERTEXT -> setOrderText(bytes)
                SET_SLIPPAGE -> setSlippage(bytes)
                SET_LIMIT -> setLimit(bytes)
                GET_ACCOUNT -> getAccount(bytes)
                GET_DIGITS -> getDigits(bytes)
                GET_MAXLOT -> getMaxLot(bytes)
                GET_MINLOT -> getMinLot(bytes)
                GET_MARGININIT -> getMarginInit(bytes)
                GET_TIME -> getTime()
                GET_TRADEALLOWED -> getTradeAllowed(bytes)
                GET_MAXTICKS -> getMaxTicks()
                GET_SERVERSTATE -> getServerState()
                else -> IO.monad().just(BROKER_COMMAND_UNAVAILABLE)
            }
        }
        out_CommandResultToFill[0] = runDirect(commandCall)
    }
}
