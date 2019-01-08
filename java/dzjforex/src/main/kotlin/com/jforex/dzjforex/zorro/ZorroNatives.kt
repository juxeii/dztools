package com.jforex.dzjforex.zorro

import com.jforex.dzjforex.misc.logger
import com.sun.corba.se.impl.activation.ServerMain

class ZorroNatives {
    fun printErrorOnZorro(message: String) {
        jcallback_BrokerError(message)
    }

    fun logAndPrintErrorOnZorro(message: String) {
        logger.error(message)
        printErrorOnZorro(message)
    }

    fun showInZorroWindow(errorMsg: String) = jcallback_BrokerError(errorMsg)

    fun logToZorroFile(errorMsg: String) = showInZorroWindow("#$errorMsg")

    fun zorroPopup(errorMsg: String) = ServerMain.logError("!$errorMsg")

    external fun jcallback_BrokerError(errorMsg: String): Int

    external fun jcallback_BrokerProgress(progress: Int): Int

    external fun triggerQuoteReq()
}