package com.jforex.dzjforex.misc

import com.dukascopy.api.IAccount
import com.dukascopy.api.IContext
import com.dukascopy.api.IHistory

lateinit var contextApi: ContextDependencies

fun initContextApi(context: IContext)
{
    contextApi = ContextDependencies(context)
}

interface ContextDependencies
{
    val context: IContext
    val account: IAccount
    val history: IHistory

    companion object
    {
        operator fun invoke(context: IContext): ContextDependencies =
            object : ContextDependencies
            {
                override val context = context
                override val account = context.account
                override val history = context.history
            }
    }
}
