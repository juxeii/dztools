package com.jforex.dzjforex.account

import com.dukascopy.api.IAccount
import com.dukascopy.api.Instrument
import com.dukascopy.api.OfferSide
import com.jforex.dzjforex.misc.ContextDependencies
import com.jforex.kforexutils.misc.asCost

object AccountApi
{
    fun <F> ContextDependencies<F>.leverage() = account.leverage

    fun <F> ContextDependencies<F>.baseEequity() = invoke { account.baseEquity }

    fun <F> ContextDependencies<F>.tradeVal() = invoke { account.equity - account.baseEquity }

    fun <F> ContextDependencies<F>.equity() = account.equity

    fun <F> ContextDependencies<F>.state(): IAccount.AccountState = account.accountState

    fun <F> ContextDependencies<F>.usedMargin() = invoke { account.usedMargin }

    fun <F> ContextDependencies<F>.freeMargin() = account.creditLine / leverage()

    fun <F> ContextDependencies<F>.pipCost(instrument: Instrument): Double
    {
        val pipCost = context
            .utils
            .convertPipToCurrency(instrument, account.accountCurrency, OfferSide.ASK) * instrument.minTradeAmount
        return pipCost.asCost()
    }

    fun <F> ContextDependencies<F>.isTradingAllowed() =
        state() == IAccount.AccountState.OK || state() == IAccount.AccountState.OK_NO_MARGIN_CALL

    fun <F> ContextDependencies<F>.accountName() = invoke { account.accountId }
}
