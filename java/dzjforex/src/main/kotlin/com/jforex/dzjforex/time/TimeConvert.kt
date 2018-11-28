package com.jforex.dzjforex.time

fun toDATEFormatInSeconds(timeInMs: Long) = toDATEFormat(timeInMs / 1000L)
fun toDATEFormat(timeInMs: Long) = timeInMs.toDouble() / (24.0 * 60.0 * 60.0) + 25569.0
fun toLongFormat(date: Double) = ((date - 25569.0) * 24.0 * 60.0 * 60.0).toLong()