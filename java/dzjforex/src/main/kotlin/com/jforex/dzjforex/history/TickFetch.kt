package com.jforex.dzjforex.history

import com.dukascopy.api.ITick
import com.dukascopy.api.Instrument
import com.jforex.dzjforex.misc.ContextDependencies
import com.jforex.dzjforex.misc.logger
import com.jforex.dzjforex.time.asUTCTimeFormat
import com.jforex.dzjforex.time.toUTCTime
import com.jforex.kforexutils.history.retry
import com.jforex.kforexutils.instrument.spread
import io.reactivex.Observable
import io.reactivex.Single

object TickFetch
{
    fun <F> ContextDependencies<F>.fetchTicks(
        instrument: Instrument,
        startTime: Long,
        endTime: Long,
        noOfTicks: Int
    ) = bindingCatch {
        val endTickTime = getLatestTickTime(instrument, endTime).bind()
        val fetchedTicks = getTicks(
            instrument = instrument,
            startTime = startTime,
            endTime = endTickTime,
            numberOfTicks = noOfTicks
        ).bind()
        logger.debug("First stored tick time ${fetchedTicks.first().time.asUTCTimeFormat()}")
        logger.debug("Last stored tick time ${fetchedTicks.last().time.asUTCTimeFormat()}")
        BrokerHistoryData(fetchedTicks.size, fetchedTicks)
    }

    fun <F> ContextDependencies<F>.getLatestTickTime(instrument: Instrument, endTime: Long) = catch {
        minOf(history.retry { getTimeOfLastTick(instrument) }, endTime)
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
                history.retry { getTicks(instrument, fetchTimes.first, fetchTimes.second).asReversed() }
            }
            .concatMapIterable { it }
            .distinctUntilChanged { tickA, tickB -> tickA.ask == tickB.ask }
            .take(numberOfTicks.toLong())
            .takeUntil { it.time < startTime }
            .map { tick -> createT6Data(tick, instrument) }
            .toList()
            .blockingGet()
    }

    fun <F> ContextDependencies<F>.createFetchTimes(endTime: Long): Observable<Pair<Long, Long>>
    {
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
            value = instrument.spread().toFloat(),
            volume = askVolume.toFloat()
        )
    }
}
