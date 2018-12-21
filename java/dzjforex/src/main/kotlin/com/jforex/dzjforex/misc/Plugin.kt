package com.jforex.dzjforex.misc

import arrow.core.Try
import arrow.effects.DeferredK
import arrow.effects.unsafeRunAsync
import com.dukascopy.api.JFException
import com.dukascopy.api.system.ClientFactory
import com.dukascopy.api.system.IClient
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jforex.dzjforex.settings.PluginSettings
import com.jforex.dzjforex.zorro.ZorroNatives
import com.jforex.dzjforex.zorro.client
import com.jforex.dzjforex.zorro.heartBeatIndication
import com.jforex.kforexutils.client.init
import io.reactivex.Single
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.aeonbits.owner.ConfigFactory
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

val logger: Logger = LogManager.getLogger()
val pluginApi = PluginDependencies(client, ConfigFactory.create(PluginSettings::class.java), ZorroNatives())

fun getClient(): IClient
{
    var client = Single
        .fromCallable { ClientFactory.getDefaultInstance() }
        .doOnError { logger.debug("Error retrieving IClient instance! " + it.message) }
        .blockingGet()
    client.init()
    return client
}


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
        logger.debug("waiting... 1")
        val resultRelay = BehaviorRelay.create<T>()
        task.unsafeRunAsync { result ->
            logger.debug("running... 1")
            result.fold(
                { logger.debug("Error while progress progressWait task!") },
                {
                    logger.debug("running... result")
                    resultRelay.accept(it)
                })
        }
        logger.debug("waiting... 2")
        runBlocking {
            logger.debug("waiting... 3")
            while (!resultRelay.hasValue())
            {
                logger.debug("waiting... 4")
                if(natives == null){
                    logger.debug("natives is null!!")
                }
                if(pluginSettings == null){
                    logger.debug("pluginSettings is null!!")
                }
                natives.jcallback_BrokerProgress(heartBeatIndication)
                logger.debug("waiting... 5")
                delay(pluginSettings.zorroProgressInterval())
                logger.debug("waiting... 6")
            }
        }
        return resultRelay.value!!
    }
}