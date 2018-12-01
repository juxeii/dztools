package com.jforex.dzjforex.zorro

import arrow.data.Reader
import arrow.data.ReaderApi
import arrow.data.map
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jforex.dzjforex.ZorroBridge.jcallback_BrokerError
import com.jforex.dzjforex.ZorroBridge.jcallback_BrokerProgress
import com.jforex.dzjforex.misc.PluginEnvironment
import com.sun.corba.se.impl.activation.ServerMain.logError
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.apache.logging.log4j.LogManager
import java.util.concurrent.TimeUnit

private val logger = LogManager.getLogger()

internal fun <T> progressWait(task: Single<T>): Reader<PluginEnvironment, T> = ReaderApi
    .ask<PluginEnvironment>()
    .map { env ->
        val stateRelay = BehaviorRelay.create<T>()
        task
            .subscribeOn(Schedulers.io())
            .subscribe { it -> stateRelay.accept(it) }
        Observable.interval(
            0,
            env.pluginSettings.zorroProgressInterval(),
            TimeUnit.MILLISECONDS
        )
            .takeWhile { !stateRelay.hasValue() }
            .blockingSubscribe { jcallback_BrokerProgress(heartBeatIndication) }

        stateRelay.value!!
    }

internal fun logZorroError(errorMsg: String) = jcallback_BrokerError(errorMsg)

internal fun logZorroDiagnose(errorMsg: String) = logZorroError("#$errorMsg")

internal fun logZorroPopup(errorMsg: String) = logError("!$errorMsg")