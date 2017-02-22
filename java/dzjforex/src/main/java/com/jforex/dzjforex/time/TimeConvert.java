package com.jforex.dzjforex.time;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.concurrent.TimeUnit;

import com.dukascopy.api.IBar;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Period;
import com.dukascopy.api.Unit;

public class TimeConvert {

    private static SimpleDateFormat simpleUTCormat;
    private static final int DAYS_SINCE_UTC_EPOCH;
    private static final LocalDateTime ZERO_COM_TIME;
    private static final BigDecimal MILLIS_PER_DAY;

    static {
        simpleUTCormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        simpleUTCormat.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));
        DAYS_SINCE_UTC_EPOCH = 25569;
        ZERO_COM_TIME = LocalDateTime.of(1899, 12, 30, 0, 0);
        MILLIS_PER_DAY = new BigDecimal(86400000);
    }

    private TimeConvert() {
    }

    public static LocalDateTime dateTimeFromOLEDate(final double oleTime) {
        final BigDecimal comTime = BigDecimal.valueOf(oleTime);
        final BigDecimal daysAfterZero = comTime.setScale(0, RoundingMode.DOWN);
        final BigDecimal fraction = comTime
            .subtract(daysAfterZero)
            .abs();
        final BigDecimal fractionMillisAfterZero = fraction
            .multiply(MILLIS_PER_DAY)
            .setScale(0, RoundingMode.HALF_DOWN);

        return ZERO_COM_TIME
            .plusDays(daysAfterZero.intValue())
            .plusNanos(millisToNano(fractionMillisAfterZero.intValue()));
    }

    public static long millisFromOLEDate(final double oleTime) {
        return dateTimeFromOLEDate(oleTime)
            .toInstant(ZoneOffset.UTC)
            .toEpochMilli();
    }

    public static long millisFromOLEDateRoundMinutes(final double oleTime) {
        return dateTimeFromOLEDate(oleTime)
            .toInstant(ZoneOffset.UTC)
            .truncatedTo(ChronoUnit.MINUTES)
            .toEpochMilli();
    }

    public static String formatDateTime(final long dateTime) {
        return simpleUTCormat.format(new Date(dateTime));
    }

    public static String formatOLETime(final double oleTime) {
        final long dateTime = millisFromOLEDate(oleTime);
        return formatDateTime(dateTime);
    }

    public static Period getPeriodFromMinutes(final int minutes) {
        return Period.createCustomPeriod(Unit.Minute, minutes);
    }

    public static double getOLEDateFromMillis(final long millis) {
        return DAYS_SINCE_UTC_EPOCH + (double) millis / (1000 * 3600 * 24);
    }

    public static double getOLEDateFromMillisRounded(final long millis) {
        return getOLEDateFromMillis(millis) + 1e-8;
    }

    public static double getUTCTimeFromBar(final IBar bar) {
        return getOLEDateFromMillisRounded(bar.getTime());
    }

    public static double getUTCTimeFromTick(final ITick tick) {
        return getOLEDateFromMillisRounded(tick.getTime());
    }

    public static long millisToNano(final long millis) {
        return TimeUnit.MILLISECONDS.toNanos(millis);
    }
}
