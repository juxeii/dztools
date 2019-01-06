package com.jforex.dzjforex.history

import arrow.Kind
import com.dukascopy.api.ITick
import com.dukascopy.api.Instrument
import com.jforex.dzjforex.misc.ContextDependencies
import com.jforex.dzjforex.misc.getStackTrace
import com.jforex.dzjforex.misc.logger
import com.jforex.dzjforex.time.asUTCTimeFormat
import com.jforex.dzjforex.time.asUnixTimeFormat
import com.jforex.dzjforex.time.toUTCTime
import com.jforex.dzjforex.zorro.BROKER_HISTORY_UNAVAILABLE
import com.jforex.kforexutils.price.Price
import io.reactivex.Observable
import io.reactivex.Single

object TickFetchApi
{
    fun <F> ContextDependencies<F>.fetchTicks(
        instrument: Instrument,
        startTime: Long,
        endTime: Long,
        noOfTicks: Int
    ) = bindingCatch {
        logger.debug(
            "Fetching $noOfTicks ticks for $instrument \n" +
                    " startTime ${startTime.asUnixTimeFormat()}" +
                    " endTime ${endTime.asUnixTimeFormat()}"
        )

        val endTickTime = getLatesTickTime(instrument, endTime).bind()
        val fetchedTicks = getTicks(
            instrument = instrument,
            startTime = startTime,
            endTime = endTickTime,
            numberOfTicks = noOfTicks
        ).bind()
        logger.debug(
            "Fetched ${fetchedTicks.size} ticks \n" +
                    " first tick ${fetchedTicks.first()}" +
                    " last tick ${fetchedTicks.last()}"
        )
        BrokerHistoryData(fetchedTicks.size, fetchedTicks)
    }.handleError { error ->
        logger.error("FetchTicks error! ${error.message} Stack trace: ${getStackTrace(error)}")
        BrokerHistoryData(BROKER_HISTORY_UNAVAILABLE)
    }

    fun <F> ContextDependencies<F>.getLatesTickTime(instrument: Instrument, endTime: Long) =
        catch { minOf(history.getTimeOfLastTick(instrument), endTime) }

    fun <F> ContextDependencies<F>.getTicks(
        instrument: Instrument,
        startTime: Long,
        endTime: Long,
        numberOfTicks: Int
    ): Kind<F, List<T6Data>> =
        delay {
            Observable
                .defer { createStartDates(endTime) }
                .map { startDate ->
                    val endDate = startDate + pluginSettings.tickfetchmillis() - 1L
                    history.getTicks(instrument, startDate, endDate).asReversed()
                }
                .concatMapIterable { it }
                .distinctUntilChanged { tickA, tickB -> tickA.ask == tickB.ask }
                .take(numberOfTicks.toLong())
                .takeUntil { it.time < startTime }
                .map { tick -> createT6Data(tick, instrument) }
                .toList()
                .blockingGet()
        }

    fun <F> ContextDependencies<F>.createStartDates(endTime: Long) =
        Single
            .just(endTime)
            .flatMapObservable { countStreamForTickFetch(it) }

    fun <F> ContextDependencies<F>.countStreamForTickFetch(endTime: Long): Observable<Long>
    {
        val seq = generateSequence(1) { it + 1 }.map { counter ->
            endTime - counter * pluginSettings.tickfetchmillis() + 1
        }
        return Observable.fromIterable(seq.asIterable())
    }

    fun createT6Data(
        tick: ITick,
        instrument: Instrument
    ): T6Data
    {
        val t6Data = with(tick) {
            val ask = ask.toFloat()
            T6Data(
                time = time.toUTCTime(),
                high = ask,
                low = ask,
                open = ask,
                close = ask,
                value = Price(instrument, bid - ask).toDouble().toFloat(),
                volume = askVolume.toFloat()
            )
        }

        logger.debug(
            "Stored TIMW ${t6Data.time.asUTCTimeFormat()} ask ${t6Data.high} spread ${t6Data.value}"
        )
        return t6Data
    }
}
