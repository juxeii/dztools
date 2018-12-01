package com.jforex.dzjforex.zorro

import com.sun.corba.se.impl.activation.ServerMain

class ZorroNatives
{
    fun logZorroError(errorMsg: String) = jcallback_BrokerError(errorMsg)

    fun logZorroDiagnose(errorMsg: String) = logZorroError("#$errorMsg")

    fun logZorroPopup(errorMsg: String) = ServerMain.logError("!$errorMsg")

    external fun jcallback_BrokerError(errorMsg: String): Int

    external fun jcallback_BrokerProgress(progress: Int): Int
}