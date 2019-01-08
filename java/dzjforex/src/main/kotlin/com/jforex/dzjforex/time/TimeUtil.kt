package com.jforex.dzjforex.time

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

fun Long.toUTCTime() = (this / 1000) / (24.0 * 60.0 * 60.0) + 25569.0

fun Long.asUnixTimeFormat(): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
    return Instant
        .ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .format(formatter)
}

fun Double.toUnixTime(): Long {
    val unixTime = ((this - 25569.0) * 24.0 * 60.0 * 60.0 * 1000).toLong()
    return Instant.ofEpochMilli(unixTime + 500)
        .truncatedTo(ChronoUnit.SECONDS)
        .toEpochMilli()
}

fun Double.asUTCTimeFormat() = this.toUnixTime().asUnixTimeFormat()
