package com.jforex.dzjforex.zorro

import arrow.effects.IO
import arrow.effects.instances.io.monad.monad
import com.jforex.dzjforex.account.AccountApi.accountName
import com.jforex.dzjforex.account.BrokerAccountApi.brokerAccount
import com.jforex.dzjforex.account.BrokerAccountSuccess
import com.jforex.dzjforex.asset.BrokerAssetApi.brokerAsset
import com.jforex.dzjforex.asset.BrokerAssetSuccess
import com.jforex.dzjforex.asset.createBrokerAssetApi
import com.jforex.dzjforex.buy.BrokerBuyApi.brokerBuy
import com.jforex.dzjforex.buy.BrokerBuySuccess
import com.jforex.dzjforex.buy.createBrokerBuyApi
import com.jforex.dzjforex.command.BrokerCommandApi.getAccount
import com.jforex.dzjforex.command.BrokerCommandApi.getDigits
import com.jforex.dzjforex.command.BrokerCommandApi.getMarginInit
import com.jforex.dzjforex.command.BrokerCommandApi.getMaxLot
import com.jforex.dzjforex.command.BrokerCommandApi.getMaxTicks
import com.jforex.dzjforex.command.BrokerCommandApi.getMinLot
import com.jforex.dzjforex.command.BrokerCommandApi.getTime
import com.jforex.dzjforex.command.BrokerCommandApi.getTradeAllowed
import com.jforex.dzjforex.command.BrokerCommandApi.setLimit
import com.jforex.dzjforex.command.BrokerCommandApi.setOrderText
import com.jforex.dzjforex.command.BrokerCommandApi.setSlippage
import com.jforex.dzjforex.command.getBcOrderText
import com.jforex.dzjforex.command.getBcSlippage
import com.jforex.dzjforex.history.BrokerHistoryApi.brokerHistory
import com.jforex.dzjforex.init.BrokerInitApi.brokerInit
import com.jforex.dzjforex.login.LoginApi.brokerLogin
import com.jforex.dzjforex.login.LoginApi.logout
import com.jforex.dzjforex.misc.*
import com.jforex.dzjforex.sell.BrokerSellApi.brokerSell
import com.jforex.dzjforex.stop.BrokerStopApi.brokerStop
import com.jforex.dzjforex.subscribe.BrokerSubscribeApi.brokerSubscribe
import com.jforex.dzjforex.subscribe.createBrokerSubscribeApi
import com.jforex.dzjforex.time.BrokerTimeApi.brokerTime
import com.jforex.dzjforex.time.BrokerTimeSuccess
import com.jforex.dzjforex.trade.BrokerTradeApi.brokerTrade
import com.jforex.dzjforex.trade.BrokerTradeSuccess
import com.jforex.dzjforex.trade.createBrokerTradeApi

class ZorroBridge
{
    fun doLogin(
        username: String,
        password: String,
        accountType: String,
        out_AccountNamesToFill: Array<String>
    ): Int
    {
        val brokerLoginResult = runWithProgress(pluginApi.brokerLogin(username, password, accountType))
        return if (brokerLoginResult == LOGIN_FAIL) BROKER_INIT_FAIL else doInit(out_AccountNamesToFill)
    }

    private fun doInit(out_AccountNamesToFill: Array<String>): Int
    {
        val brokerInitResult = runDirect(pluginApi.brokerInit())
        if (brokerInitResult == BROKER_INIT_OK)
        {
            val iAccountNames = 0
            out_AccountNamesToFill[iAccountNames] = runDirect(contextApi.accountName())
        }
        return brokerInitResult
    }

    fun doLogout() = runDirect(pluginApi.logout())

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
        runWithProgress(createBrokerSubscribeApi().run { brokerSubscribe(assetName) })

    fun doBrokerAsset(assetName: String, out_AssetParamsToFill: DoubleArray): Int
    {
        val brokerAssetResult = runDirect(createBrokerAssetApi().brokerAsset(assetName))
        if (brokerAssetResult is BrokerAssetSuccess)
        {
            val iPrice = 0
            val iSpread = 1
            val iVolume = 2
            val iPip = 3
            val iPipCost = 4
            val iLotAmount = 5
            val iMarginCost = 6
            with(brokerAssetResult.data) {
                out_AssetParamsToFill[iPrice] = price
                out_AssetParamsToFill[iSpread] = spread
                out_AssetParamsToFill[iVolume] = volume
                out_AssetParamsToFill[iPip] = pip
                out_AssetParamsToFill[iPipCost] = pipCost
                out_AssetParamsToFill[iLotAmount] = lotAmount
                out_AssetParamsToFill[iMarginCost] = marginCost
            }
        }
        return brokerAssetResult.returnCode
    }

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

    fun doBrokerTrade(orderId: Int, out_TradeInfoToFill: DoubleArray): Int
    {
        val brokerTradeResult = runDirect(createBrokerTradeApi().brokerTrade(orderId))
        if (brokerTradeResult is BrokerTradeSuccess)
        {
            val iOpen = 0
            val iClose = 1
            val iProfit = 3
            with(brokerTradeResult.data) {
                out_TradeInfoToFill[iOpen] = open
                out_TradeInfoToFill[iClose] = close
                out_TradeInfoToFill[iProfit] = profit
            }
        }
        return brokerTradeResult.returnCode
    }

    fun doBrokerBuy2(
        assetName: String,
        contracts: Int,
        slDistance: Double,
        limitPrice: Double,
        out_BuyInfoToFill: DoubleArray
    ): Int
    {
        val brokerBuyResult = runWithProgress(
            createBrokerBuyApi().brokerBuy(
                assetName = assetName,
                contracts = contracts,
                slDistance = slDistance,
                limitPrice = limitPrice,
                slippage = getBcSlippage(),
                orderText = getBcOrderText()
            )
        )
        if (brokerBuyResult is BrokerBuySuccess)
        {
            val iPrice = 0
            out_BuyInfoToFill[iPrice] = brokerBuyResult.price
        }
        return brokerBuyResult.returnCode
    }

    fun doBrokerSell(orderId: Int, contracts: Int):Int{
        logger.debug("doBrokerSell called id $orderId")
        return runWithProgress(contextApi.brokerSell(orderId, contracts))
    }

    fun doBrokerStop(orderId: Int, slPrice: Double):Int{
        logger.debug("doBrokerStop called")
        return runWithProgress(contextApi.brokerStop(orderId, slPrice))
    }

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
                GET_TIME -> createQuoteApi(context).getTime()
                GET_TRADEALLOWED -> getTradeAllowed(bytes)
                GET_MAXTICKS -> getMaxTicks()
                else -> IO.monad().just(BROKER_COMMAND_UNAVAILABLE)
            }
        }
        out_CommandResultToFill[0] = runDirect(commandCall)
    }
}
