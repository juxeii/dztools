package com.jforex.dzjforex.misc

import com.jakewharton.rxrelay2.BehaviorRelay
import com.jforex.dzjforex.Zorro.callProgress
import com.jforex.dzjforex.settings.PluginSettings
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.apache.logging.log4j.LogManager
import java.util.concurrent.TimeUnit


class ZorroCommunication(pluginSettings: PluginSettings)
{
    private val logger = LogManager.getLogger(ZorroCommunication::class.java)
    private val heartBeat: Observable<Long> = Observable.interval(
        0,
        pluginSettings.zorroProgressInterval(),
        TimeUnit.MILLISECONDS
    )

    fun <T> progressWait(task: Single<T>): T
    {
        val stateRelay = BehaviorRelay.create<T>()
        task
            .subscribeOn(Schedulers.io())
            .subscribe { it -> stateRelay.accept(it) }
        heartBeat
            .takeWhile { !stateRelay.hasValue() }
            .blockingSubscribe { callProgress(heartBeatIndication) }

        return stateRelay.value!!
    }
}