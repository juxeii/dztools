package com.jforex.dzjforex.misc

import arrow.Kind
import arrow.core.Try
import arrow.effects.*
import arrow.effects.instances.io.monadDefer.monadDefer
import arrow.effects.typeclasses.MonadDefer
import com.dukascopy.api.Instrument
import com.dukascopy.api.JFException
import com.dukascopy.api.system.ClientFactory
import com.dukascopy.api.system.IClient
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jforex.dzjforex.misc.PluginApi.progressWait
import com.jforex.dzjforex.settings.PluginSettings
import com.jforex.dzjforex.zorro.ZorroNatives
import com.jforex.dzjforex.zorro.heartBeatIndication
import com.jforex.dzjforex.zorro.lotScale
import com.jforex.kforexutils.client.init
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.aeonbits.owner.ConfigFactory
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.PrintWriter
import java.io.StringWriter

val logger: Logger = LogManager.getLogger()
val pluginApi = PluginDependencies(
    getClient(),
    ConfigFactory.create(PluginSettings::class.java),
    ZorroNatives(),
    IO.monadDefer()
)

fun getClient(): IClient =
    Try {
        val client = ClientFactory.getDefaultInstance()
        client.init()
        client
    }.fold({
        logger.error("Error retrieving IClient instance! ${it.message}")
        throw(JFException("Error retrieving IClient instance! ${it.message}"))
    }) { it }

fun getStackTrace(it: Throwable): String
{
    val ex = Exception(it)
    val writer = StringWriter()
    val printWriter = PrintWriter(writer)
    ex.printStackTrace(printWriter)
    printWriter.flush()

    return writer.toString()
}

fun <D> runDirect(kind: Kind<ForIO, D>) = kind.fix().unsafeRunSync()

fun <D> runWithProgress(kind: Kind<ForIO, D>) = pluginApi.progressWait(DeferredK { runDirect(kind) })

fun Int.toAmount() = Math.abs(this) / lotScale

fun Double.toContracts() = (this * lotScale).toInt()

sealed class PluginException() : Throwable()
{
    data class AssetNotTradeable(val instrument: Instrument) : PluginException()
    data class OrderIdNotFound(val orderId: Int) : PluginException()
}
typealias AssetNotTradeableException = PluginException.AssetNotTradeable
typealias OrderIdNotFoundException = PluginException.OrderIdNotFound

interface PluginDependencies<F> : MonadDefer<F>
{
    val client: IClient
    val pluginSettings: PluginSettings
    val natives: ZorroNatives

    fun printOnZorro(message: String)
    {
        natives.jcallback_BrokerError(message)
    }

    companion object
    {
        operator fun <F> invoke(
            client: IClient,
            pluginSettings: PluginSettings,
            natives: ZorroNatives,
            MD: MonadDefer<F>
        ): PluginDependencies<F> =
            object : PluginDependencies<F>, MonadDefer<F> by MD
            {
                override val client = client
                override val pluginSettings = pluginSettings
                override val natives = natives
            }
    }
}

object PluginApi
{
    fun <F> PluginDependencies<F>.isConnected() = delay { client.isConnected }

    fun <F, T> PluginDependencies<F>.progressWait(task: DeferredK<T>): T
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