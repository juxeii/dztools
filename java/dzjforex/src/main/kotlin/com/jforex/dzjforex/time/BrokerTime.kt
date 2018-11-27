package com.jforex.dzjforex.time

import com.dukascopy.api.IContext
import com.dukascopy.api.system.IClient
import com.jforex.dzjforex.account.AccountInfo
import com.jforex.dzjforex.zorro.CONNECTION_LOST_NEW_LOGIN_REQUIRED
import com.jforex.dzjforex.zorro.CONNECTION_OK
import com.jforex.dzjforex.zorro.CONNECTION_OK_BUT_MARKET_CLOSED
import com.jforex.dzjforex.zorro.CONNECTION_OK_BUT_TRADING_NOT_ALLOWED

class BrokerTime(
    private val client: IClient,
    private val context: IContext,
    private val accountInfo: AccountInfo
) {

    fun get(out_ServerTimeToFill: DoubleArray): Int {
        if (!client.isConnected) return CONNECTION_LOST_NEW_LOGIN_REQUIRED
        if (!accountInfo.isTradingAllowed()) return CONNECTION_OK_BUT_TRADING_NOT_ALLOWED

        return getWhenConnected(out_ServerTimeToFill)
    }

    private fun getWhenConnected(out_ServerTimeToFill: DoubleArray): Int {
        val serverTime = context.time
        return if (isMarketClosed(serverTime)) CONNECTION_OK_BUT_MARKET_CLOSED
        else {
            out_ServerTimeToFill[0] = TimeConvertOLE.toDATEFormat(serverTime)
            CONNECTION_OK
        }
    }

    private fun isMarketClosed(serverTime: Long) = context
        .dataService
        .isOfflineTime(serverTime)
}