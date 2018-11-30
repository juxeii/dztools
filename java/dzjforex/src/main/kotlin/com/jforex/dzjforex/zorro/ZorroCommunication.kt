package com.jforex.dzjforex.zorro

import com.jakewharton.rxrelay2.BehaviorRelay
import com.jforex.dzjforex.ZorroBridge.jcallback_BrokerError
import com.jforex.dzjforex.ZorroBridge.jcallback_BrokerProgress
import com.jforex.dzjforex.settings.PluginSettings
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
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

    fun callProgress(progress: Int) = jcallback_BrokerProgress(progress)

    fun logError(
        errorMsg: String,
        logger: Logger
    ): Int
    {
        logger.error(errorMsg)
        return logError(errorMsg)
    }

    fun logError(errorMsg: String) = jcallback_BrokerError(errorMsg)

    fun logDiagnose(errorMsg: String) = logError("#$errorMsg")

    fun logPopUp(errorMsg: String) = logError("!$errorMsg")

    fun indicateError() = logError("Severe error occured, check dzplugin.log logfile!")

    fun showError(errorMsg: String) = logError(errorMsg)
}
