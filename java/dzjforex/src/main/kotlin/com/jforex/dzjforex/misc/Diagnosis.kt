package com.jforex.dzjforex.misc

fun printHeapInfo()
{
    logger.debug(
        "heapSize ${Runtime.getRuntime().totalMemory()}\n " +
                "heapMaxSize ${Runtime.getRuntime().maxMemory()}\n " +
                "heapFreeSize ${Runtime.getRuntime().freeMemory()}"
    )
}