package com.jforex.dzjforex.test.mock

import com.dukascopy.api.IAccount
import com.dukascopy.api.IContext
import com.dukascopy.api.IEngine
import com.dukascopy.api.IHistory
import com.jforex.dzjforex.misc.ContextDependencies
import com.jforex.dzjforex.misc.PluginDependencies
import io.mockk.mockk

interface ContextDependenciesForTest<F> : ContextDependencies<F>
{
    companion object
    {
        operator fun <F> invoke(pluginDependencies: PluginDependenciesForTest<F>): ContextDependenciesForTest<F> =
            object : ContextDependenciesForTest<F>, PluginDependencies<F> by pluginDependencies
            {
                override val jfContext = mockk<IContext>()
                override val engine = mockk<IEngine>()
                override val account = mockk<IAccount>()
                override val history = mockk<IHistory>()
            }
    }
}

fun getContextDependenciesForTest_IO() = ContextDependenciesForTest(getPluginDependenciesForTest_IO())
