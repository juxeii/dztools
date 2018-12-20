package com.jforex.dzjforex.subscription

import arrow.Kind
import arrow.typeclasses.binding
import com.dukascopy.api.Instrument
import com.dukascopy.api.JFException
import com.jforex.dzjforex.account.AccountDependencies
import com.jforex.dzjforex.history.HistoryApi.waitForLatestQuote
import com.jforex.dzjforex.history.HistoryDependencies
import com.jforex.dzjforex.misc.ContextApi.getSubscribedInstruments
import com.jforex.dzjforex.misc.ContextApi.setSubscribedInstruments
import com.jforex.dzjforex.misc.ContextDependencies
import com.jforex.dzjforex.misc.QuoteProviderDependencies
import com.jforex.dzjforex.misc.QuotesApi.getQuote
import com.jforex.dzjforex.misc.QuotesApi.hasQuote
import com.jforex.dzjforex.misc.forexInstrumentFromAssetName
import com.jforex.kforexutils.instrument.InstrumentFactory
import com.jforex.kforexutils.instrument.currencies
import com.jforex.kforexutils.price.TickQuote
import org.apache.logging.log4j.LogManager

private val logger = LogManager.getLogger()

interface BrokerSubscribeDependencies<F> : HistoryDependencies<F>,
    ContextDependencies,
    AccountDependencies,
    QuoteProviderDependencies
{
    companion object
    {
        operator fun <F> invoke(
            historyDeps: HistoryDependencies<F>,
            contextDependencies: ContextDependencies,
            accountDependencies: AccountDependencies,
            quoteProviderDependencies: QuoteProviderDependencies
        ): BrokerSubscribeDependencies<F> =
            object : BrokerSubscribeDependencies<F>,
                HistoryDependencies<F> by historyDeps,
                ContextDependencies by contextDependencies,
                AccountDependencies by accountDependencies,
                QuoteProviderDependencies by quoteProviderDependencies
            {
                override val pluginSettings = historyDeps.pluginSettings

            }
    }
}

object BrokerSubscribeApi
{
    fun <F> BrokerSubscribeDependencies<F>.subscribeInstrument(assetName: String): Kind<F, Set<Instrument>> =
        binding {
            forexInstrumentFromAssetName(assetName)
                .fold({
                    raiseError<JFException>(it)
                    emptySet<Instrument>()
                }) {
                    val instrumentsToSubscribe = getInstrumentsToSubscribe(it).bind()
                    subscribeAllInstruments(instrumentsToSubscribe).bind()
                    instrumentsToSubscribe
                }
        }

    fun <F> BrokerSubscribeDependencies<F>.getInstrumentWithCrosses(instrument: Instrument): Kind<F, Set<Instrument>>
    {
        val accountCurrency = account.accountCurrency
        val currencies = instrument.currencies + accountCurrency
        return just(InstrumentFactory.fromCombinedCurrencies(currencies))
    }

    fun <F> BrokerSubscribeDependencies<F>.getInstrumentsToSubscribe(instrument: Instrument): Kind<F, Set<Instrument>> =
        binding {
            val instrumentWithCrosses = getInstrumentWithCrosses(instrument).bind()
            val subscribedInstruments = getSubscribedInstruments()
            instrumentWithCrosses - subscribedInstruments
        }

    fun <F> BrokerSubscribeDependencies<F>.subscribeAllInstruments(instrumentsToSubscribe: Set<Instrument>)
            : Kind<F, Unit> =
        binding {
            val subscribedInstruments = getSubscribedInstruments()
            val resultingInstruments = subscribedInstruments + instrumentsToSubscribe
            setSubscribedInstruments(instrumentsToSubscribe)
        }

    fun <F> BrokerSubscribeDependencies<F>.waitForLatestQuotes(instruments: Set<Instrument>): Kind<F, List<TickQuote>> =
        binding {
            val quotes = mutableListOf<TickQuote>()
            instruments.forEach { instrument ->
                if (hasQuote(instrument))
                {
                    logger.debug("Quote for $instrument is available in quoteProvider")
                    quotes.plus(getQuote(instrument))
                } else
                {
                    logger.debug("Quote for $instrument is not available in quoteProvider, looking up in history")
                    quotes.plus(waitForLatestQuote(instrument).bind())
                }
            }
            quotes
        }
}
