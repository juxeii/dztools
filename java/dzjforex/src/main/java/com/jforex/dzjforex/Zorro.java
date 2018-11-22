package com.jforex.dzjforex;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Zorro {


    private final static int heartBeatIndication = 1;
    private final static Logger logger = LogManager.getLogger(Zorro.class);

    public static int callProgress(final int progress) {
        return jcallback_BrokerProgress(progress);
    }

    public static int logError(final String errorMsg,
                               final Logger logger) {
        logger.error(errorMsg);
        return logError(errorMsg);
    }

    public static int logError(final String errorMsg) {
        return jcallback_BrokerError(errorMsg);
    }

    public static void logDiagnose(final String errorMsg) {
        logError("#" + errorMsg);
    }

    public static void logPopUp(final String errorMsg) {
        logError("!" + errorMsg);
    }

    public static void indicateError() {
        logError("Severe error occured, check dzplugin.log logfile!");
    }

    public static void showError(final String errorMsg) {
        logError(errorMsg);
    }

    private static native int jcallback_BrokerError(String errorMsg);

    private static native int jcallback_BrokerProgress(int progress);
}
