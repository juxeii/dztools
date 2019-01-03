package com.jforex.dzjforex.account

import com.dukascopy.api.IAccount
import com.dukascopy.api.Instrument
import com.dukascopy.api.OfferSide
import com.jforex.dzjforex.misc.ContextDependencies
import com.jforex.kforexutils.misc.asCost

object AccountApi
{
    fun <F> ContextDependencies<F>.baseEequity() = delay { account.baseEquity }

    fun <F> ContextDependencies<F>.tradeVal() = delay { account.equity - account.baseEquity }

    fun <F> ContextDependencies<F>.usedMargin() = delay { account.usedMargin }

    fun <F> ContextDependencies<F>.pipCost(instrument: Instrument) = delay {
        val pipCost = jfContext
            .utils
            .convertPipToCurrency(instrument, account.accountCurrency, OfferSide.ASK) * instrument.minTradeAmount
        pipCost.asCost()
    }

    fun <F> ContextDependencies<F>.isTradingAllowed() = delay {
        account.accountState == IAccount.AccountState.OK ||
                account.accountState == IAccount.AccountState.OK_NO_MARGIN_CALL
    }

    fun <F> ContextDependencies<F>.accountName() = delay { account.accountId }
}
