package com.jforex.dzjforex;

public class ZorroLogger {

    public static int log(String errorMsg) {
        return jcallback_BrokerError(errorMsg);
    }

    public static int logProgress(int progress) {
        return jcallback_BrokerProgress(progress);
    }

    public static void logDiagnose(String errorMsg) {
        log("#" + errorMsg);
    }

    public static void logPopUp(String errorMsg) {
        log("!" + errorMsg);
    }

    public static void indicateError() {
        log("Error! Check dzpjforex logfile!");
    }

    public static void showError(String errorMsg) {
        log(errorMsg);
    }

    private static native int jcallback_BrokerError(String errorMsg);

    private static native int jcallback_BrokerProgress(int progress);
}
