package com.jforex.dzjforex.history

import com.dukascopy.api.ITick
import com.dukascopy.api.Instrument
import com.dukascopy.api.OfferSide
import com.jforex.dzjforex.misc.ContextDependencies
import com.jforex.dzjforex.misc.logger
import com.jforex.dzjforex.time.asUTCTimeFormat
import com.jforex.dzjforex.time.toUTCTime
import com.jforex.kforexutils.history.retry
import com.jforex.kforexutils.price.Price
import io.reactivex.Observable
import io.reactivex.Single

object TickFetch {
    fun <F> ContextDependencies<F>.fetchTicks(
        instrument: Instrument,
        startTime: Long,
        endTime: Long,
        noOfTicks: Int
    ) = bindingCatch {
        val endTickTime = getLatesTickTime(instrument, endTime).bind()
        val fetchedTicks = getTicks(
            instrument = instrument,
            startTime = startTime,
            endTime = endTickTime,
            numberOfTicks = noOfTicks
        ).bind()
        BrokerHistoryData(fetchedTicks.size, fetchedTicks)
    }

    fun <F> ContextDependencies<F>.getLatesTickTime(instrument: Instrument, endTime: Long) = catch {
        minOf(history.retry { getTimeOfLastTick(instrument)}, endTime)
    }

    fun <F> ContextDependencies<F>.getTicks(
        instrument: Instrument,
        startTime: Long,
        endTime: Long,
        numberOfTicks: Int
    ) = delay {
        Observable
            .defer { createFetchTimes(endTime) }
            .map { fetchTimes ->
                history.retry { getTicks(instrument, fetchTimes.first, fetchTimes.second).asReversed()}
            }
            .concatMapIterable { it }
            .distinctUntilChanged { tickA, tickB -> tickA.ask == tickB.ask }
            .take(numberOfTicks.toLong())
            .takeUntil { it.time < startTime }
            .map { tick ->
                val t6Data = createT6Data(tick, instrument)
                logger.debug(
                    "Stored bar time ${t6Data.time.asUTCTimeFormat()} ask ${t6Data.high} spread ${t6Data.value}"
                )
                t6Data
            }
            .toList()
            .blockingGet()
    }

    fun <F> ContextDependencies<F>.createFetchTimes(endTime: Long): Observable<Pair<Long, Long>> {
        val seq = generateSequence(1) { it + 1 }.map { counter ->
            val startTickTime = endTime - counter * pluginSettings.tickfetchmillis() + 1L
            val endTickTime = startTickTime + pluginSettings.tickfetchmillis() - 1L
            Pair(startTickTime, endTickTime)
        }
        return Single
            .just(endTime)
            .flatMapObservable { Observable.fromIterable(seq.asIterable()) }
    }

    fun createT6Data(tick: ITick, instrument: Instrument) = with(tick) {
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
}
