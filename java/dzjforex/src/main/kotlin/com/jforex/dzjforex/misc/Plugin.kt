package com.jforex.dzjforex.misc

import arrow.effects.DeferredK
import arrow.effects.unsafeRunAsync
import com.dukascopy.api.system.IClient
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jforex.dzjforex.settings.PluginSettings
import com.jforex.dzjforex.zorro.ZorroNatives
import com.jforex.dzjforex.zorro.heartBeatIndication
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.aeonbits.owner.ConfigFactory
import org.apache.logging.log4j.LogManager

private val logger = LogManager.getLogger()

val client = getClient()
val natives = ZorroNatives()
val pluginApi = PluginDependencies(client, ConfigFactory.create(PluginSettings::class.java), natives)

interface PluginDependencies
{
    val client: IClient
    val pluginSettings: PluginSettings
    val natives: ZorroNatives

    companion object
    {
        operator fun invoke(
            client: IClient,
            pluginSettings: PluginSettings,
            natives: ZorroNatives
        ): PluginDependencies =
            object : PluginDependencies
            {
                override val client = client
                override val pluginSettings = pluginSettings
                override val natives = natives
            }
    }
}

object PluginApi
{
    fun <T> PluginDependencies.progressWait(task: DeferredK<T>): T
    {
        val resultRelay = BehaviorRelay.create<T>()
        task.unsafeRunAsync { result ->
            result.fold(
                { logger.debug("Error while progress progressWait task!") },
                { resultRelay.accept(it) })
        }
        runBlocking {
            while (!resultRelay.hasValue())
            {
                natives.jcallback_BrokerProgress(heartBeatIndication)
                delay(pluginSettings.zorroProgressInterval())
            }
        }
        return resultRelay.value!!
    }
}