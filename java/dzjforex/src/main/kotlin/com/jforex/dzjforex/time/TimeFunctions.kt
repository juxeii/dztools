package com.jforex.dzjforex.time

fun toDATEFormat(time: Long) = time.toDouble() / (24.0 * 60.0 * 60.0) + 25569.0
fun toLongFormat(date: Double) = ((date - 25569.0) * 24.0 * 60.0 * 60.0).toLong()