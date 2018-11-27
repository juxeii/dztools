package com.jforex.dzjforex.time

import java.util.concurrent.TimeUnit
import com.dukascopy.api.ITick
import com.dukascopy.api.IBar
import com.dukascopy.api.Period
import java.time.temporal.ChronoUnit
import java.time.ZoneOffset
import java.math.RoundingMode
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.SimpleTimeZone
import com.dukascopy.charts.utils.formatter.DateFormatter.setTimeZone
import java.text.SimpleDateFormat
import com.dukascopy.api.Unit

object TimeUtil
{
    private var simpleUTCormat: SimpleDateFormat? = null
    private val DAYS_SINCE_UTC_EPOCH: Int
    private val ZERO_COM_TIME: LocalDateTime
    private val MILLIS_PER_DAY: BigDecimal

    init
    {
        simpleUTCormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
        simpleUTCormat!!.timeZone = SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC")
        DAYS_SINCE_UTC_EPOCH = 25569
        ZERO_COM_TIME = LocalDateTime.of(1899, 12, 30, 0, 0)
        MILLIS_PER_DAY = BigDecimal(86400000)
    }

    fun dateTimeFromOLEDate(oleTime: Double): LocalDateTime
    {
        val comTime = BigDecimal.valueOf(oleTime)
        val daysAfterZero = comTime.setScale(0, RoundingMode.DOWN)
        val fraction = comTime
            .subtract(daysAfterZero)
            .abs()
        val fractionMillisAfterZero = fraction
            .multiply(MILLIS_PER_DAY)
            .setScale(0, RoundingMode.HALF_DOWN)

        return ZERO_COM_TIME
            .plusDays(daysAfterZero.toInt().toLong())
            .plusNanos(millisToNano(fractionMillisAfterZero.toInt().toLong()))
    }

    fun millisFromOLEDate(oleTime: Double): Long
    {
        return dateTimeFromOLEDate(oleTime)
            .toInstant(ZoneOffset.UTC)
            .toEpochMilli()
    }

    fun millisFromOLEDateRoundMinutes(oleTime: Double): Long
    {
        return dateTimeFromOLEDate(oleTime)
            .toInstant(ZoneOffset.UTC)
            .truncatedTo(ChronoUnit.MINUTES)
            .toEpochMilli()
    }

    fun getPeriodFromMinutes(minutes: Int): Period
    {
        return Period.createCustomPeriod(Unit.Minute, minutes)
    }

    fun getOLEDateFromMillis(millis: Long): Double
    {
        return DAYS_SINCE_UTC_EPOCH + millis.toDouble() / (1000 * 3600 * 24)
    }

    fun getOLEDateFromMillisRounded(millis: Long): Double
    {
        return getOLEDateFromMillis(millis) + 1e-8
    }

    fun getUTCTimeFromBar(bar: IBar): Double
    {
        return getOLEDateFromMillisRounded(bar.time)
    }

    fun getUTCTimeFromTick(tick: ITick): Double
    {
        return getOLEDateFromMillisRounded(tick.time)
    }

    fun millisToNano(millis: Long): Long
    {
        return TimeUnit.MILLISECONDS.toNanos(millis)
    }
}