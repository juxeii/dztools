package com.jforex.dzjforex;

public class ZorroLogger {

    public static int log(final String errorMsg) {
        return jcallback_BrokerError(errorMsg);
    }

    public static int logProgress(final int progress) {
        return jcallback_BrokerProgress(progress);
    }

    public static void logDiagnose(final String errorMsg) {
        log("#" + errorMsg);
    }

    public static void logPopUp(final String errorMsg) {
        log("!" + errorMsg);
    }

    public static void indicateError() {
        log("Severe error occured, check dzplugin.log logfile!");
    }

    public static void showError(final String errorMsg) {
        log(errorMsg);
    }

    private static native int jcallback_BrokerError(String errorMsg);

    private static native int jcallback_BrokerProgress(int progress);
}
