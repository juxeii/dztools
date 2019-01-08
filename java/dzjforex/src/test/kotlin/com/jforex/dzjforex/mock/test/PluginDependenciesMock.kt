package com.jforex.dzjforex.mock.test

import arrow.effects.IO
import arrow.effects.instances.io.monadDefer.monadDefer
import arrow.effects.typeclasses.MonadDefer
import com.dukascopy.api.system.IClient
import com.jforex.dzjforex.misc.PluginDependencies
import com.jforex.dzjforex.settings.PluginSettings
import com.jforex.dzjforex.zorro.ZorroNatives
import io.mockk.mockk

interface PluginDependenciesForTest<F> : PluginDependencies<F>
{
    companion object
    {
        operator fun <F> invoke(MD: MonadDefer<F>): PluginDependenciesForTest<F> =
            object : PluginDependenciesForTest<F>, MonadDefer<F> by MD
            {
                override val client = mockk<IClient>()
                override val pluginSettings = mockk<PluginSettings>()
                override val natives = mockk<ZorroNatives>()
            }
    }
}

fun getPluginDependenciesForTest_IO() = PluginDependenciesForTest(IO.monadDefer())
