package com.jforex.dzjforex.quote

import arrow.Kind
import arrow.typeclasses.binding
import com.jforex.dzjforex.history.HistoryApi.fetchLatestQuote
import com.jforex.dzjforex.misc.ContextApi.getSubscribedInstruments
import com.jforex.dzjforex.misc.QuoteDependencies
import com.jforex.dzjforex.quote.QuotesProviderApi.hasQuote

object FetchQuotesApi
{
    fun <F> QuoteDependencies<F>.fetchLatestQuotes(): Kind<F, Quotes> = binding {
        getInstrumentsWithNoQuotes()
            .bind()
            .map { instrument -> fetchLatestQuote(instrument).bind() }
            .map { quote -> Pair(quote.instrument, quote) }
            .toMap()
    }

    fun <F> QuoteDependencies<F>.getInstrumentsWithNoQuotes() =
        getSubscribedInstruments().map { it.filter { instrument -> !hasQuote(instrument) }.toSet() }
}