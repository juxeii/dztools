package com.jforex.dzjforex.account

import com.dukascopy.api.IAccount
import com.dukascopy.api.Instrument
import com.dukascopy.api.OfferSide
import com.jforex.dzjforex.misc.ContextDependencies
import com.jforex.kforexutils.misc.asCost

object AccountApi
{
    fun <F> ContextDependencies<F>.baseEequity() = invoke { account.baseEquity }

    fun <F> ContextDependencies<F>.tradeVal() = invoke { account.equity - account.baseEquity }

    fun <F> ContextDependencies<F>.usedMargin() = invoke { account.usedMargin }

    fun <F> ContextDependencies<F>.pipCost(instrument: Instrument) = invoke {
        val pipCost = jfContext
            .utils
            .convertPipToCurrency(instrument, account.accountCurrency, OfferSide.ASK) * instrument.minTradeAmount
        pipCost.asCost()
    }

    fun <F> ContextDependencies<F>.isTradingAllowed() = invoke {
        account.accountState == IAccount.AccountState.OK ||
                account.accountState == IAccount.AccountState.OK_NO_MARGIN_CALL
    }

    fun <F> ContextDependencies<F>.accountName() = invoke { account.accountId }
}
