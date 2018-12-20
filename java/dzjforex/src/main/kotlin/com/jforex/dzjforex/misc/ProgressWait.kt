package com.jforex.dzjforex.misc

import arrow.Kind
import arrow.effects.DeferredK
import arrow.effects.typeclasses.MonadDefer
import arrow.effects.unsafeRunAsync
import com.dukascopy.api.system.IClient
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jforex.dzjforex.settings.PluginSettings
import com.jforex.dzjforex.zorro.ZorroNatives
import com.jforex.dzjforex.zorro.heartBeatIndication
import com.jforex.dzjforex.zorro.realLoginType
import com.jforex.kforexutils.authentification.LoginCredentials
import com.jforex.kforexutils.authentification.LoginType
import com.jforex.kforexutils.client.login
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.LogManager
import javax.swing.text.html.parser.DTDConstants.MD

private val logger = LogManager.getLogger()

interface ProgressWaitDependencies
{
    val pluginSettings: PluginSettings
    val natives: ZorroNatives

    companion object
    {
        operator fun invoke(pluginSettings: PluginSettings, natives: ZorroNatives): ProgressWaitDependencies =
            object : ProgressWaitDependencies
            {
                override val pluginSettings = pluginSettings
                override val natives = natives
            }
    }
}

object ProgressWaitApi
{
    fun <T> ProgressWaitDependencies.wait(task: DeferredK<T>): T
    {
        val stateRelay = BehaviorRelay.create<T>()
        task.unsafeRunAsync { result ->
            result.fold(
                { logger.debug("Error while progress wait task!") },
                { stateRelay.accept(it) })
        }
        runBlocking {
            while (!stateRelay.hasValue())
            {
                natives.jcallback_BrokerProgress(heartBeatIndication)
                delay(pluginSettings.zorroProgressInterval())
            }
        }
        return stateRelay.value!!
    }
}