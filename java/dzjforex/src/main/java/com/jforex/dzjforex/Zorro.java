package com.jforex.dzjforex;

import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jforex.dzjforex.config.PluginConfig;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

public class Zorro {

    private final PluginConfig pluginConfig;

    private final static Logger logger = LogManager.getLogger(Zorro.class);

    public Zorro(final PluginConfig pluginConfig) {
        this.pluginConfig = pluginConfig;
    }

    public void progressWait(final BooleanSupplier condition) {
        while (condition.getAsBoolean()) {
            callProgress(1);
            Observable
                .timer(pluginConfig.zorroProgressInterval(),
                       TimeUnit.MILLISECONDS,
                       Schedulers.io())
                .blockingFirst();
        }
    }

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
