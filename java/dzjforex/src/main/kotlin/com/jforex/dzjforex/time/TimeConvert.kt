package com.jforex.dzjforex.time

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun toDATEFormat(timeInMs: Long) = (timeInMs / 1000) / (24.0 * 60.0 * 60.0) + 25569.0

fun toLongFormat(date: Double) = ((date - 25569.0) * 24.0 * 60.0 * 60.0 * 1000).toLong()

fun formatUnixTime(unixTime: Long): String
{
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
    return Instant
        .ofEpochMilli(unixTime)
        .atZone(ZoneId.systemDefault())
        .format(formatter);
}

fun formatUTCTime(date: Double) = formatUnixTime(toLongFormat(date))