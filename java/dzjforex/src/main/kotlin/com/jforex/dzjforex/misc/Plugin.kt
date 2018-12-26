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
import com.jforex.dzjforex.zorro.heartBeatIndication
import com.jforex.kforexutils.client.init
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.aeonbits.owner.ConfigFactory
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

val logger: Logger = LogManager.getLogger()
val pluginApi = PluginDependencies(getClient(), ConfigFactory.create(PluginSettings::class.java), ZorroNatives())

fun getClient(): IClient =
    Try { ClientFactory.getDefaultInstance() }
        .fold({
            logger.debug("Error retrieving IClient instance! ${it.message}")
            throw(JFException("Error retrieving IClient instance! ${it.message}"))
        }) { client ->
            client.init()
            client
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
    fun PluginDependencies.isConnected() = client.isConnected

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

    fun PluginDependencies.contractsToAmount(contracts: Int) = Math.abs(contracts) / pluginSettings.lotScale()

    fun PluginDependencies.amountToContracts(amount: Double) = (amount * pluginSettings.lotScale()).toInt()
}