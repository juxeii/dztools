package com.jforex.dzjforex.misc

import com.dukascopy.api.IContext
import com.dukascopy.api.Instrument

interface ContextDependencies
{
    val context: IContext

    companion object
    {
        operator fun invoke(context: IContext): ContextDependencies =
            object : ContextDependencies
            {
                override val context = context
            }
    }
}

object ContextApi
{
    fun ContextDependencies.getSubscribedInstruments() = context.subscribedInstruments

    fun ContextDependencies.setSubscribedInstruments(instrumentsToSubscribe: Set<Instrument>)
    {
        context.setSubscribedInstruments(instrumentsToSubscribe, true)
    }
}