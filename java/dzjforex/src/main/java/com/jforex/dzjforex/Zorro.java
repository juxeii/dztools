package com.jforex.dzjforex;

import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jforex.dzjforex.config.PluginConfig;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class Zorro {

    private final PluginConfig pluginConfig;
    private final Observable<Long> progressObservable;
    private Disposable progressDisposable;
    private static int progressValue = 1;

    private final static Logger logger = LogManager.getLogger(Zorro.class);

    public Zorro(final PluginConfig pluginConfig) {
        this.pluginConfig = pluginConfig;

        progressObservable = Observable
            .interval(pluginConfig.zorroProgressInterval(),
                      TimeUnit.MILLISECONDS,
                      Schedulers.io())
            .doOnSubscribe(d -> logger.debug("Starting progress interval..."))
            .doOnTerminate(() -> logger.debug("Progress interval stopped."));
    }

    public void startProgressInterval() {
        progressDisposable = progressObservable
            .subscribe(tick -> {
                final int progressResult = callProgress(progressValue);
                logger.debug("Progress tick " + tick + " was sent with result " + progressResult);
            });
    }

    public void stopProgressInterval() {
        logger.debug("stopProgressInterval called");
        if (progressDisposable != null && !progressDisposable.isDisposed()) {
            progressDisposable.dispose();
            logger.debug("Progress interval stopped.");
        }
    }

    public static int logError(final String errorMsg,
                               final Logger logger) {
        logger.error(errorMsg);
        return logError(errorMsg);
    }

    public static int logError(final String errorMsg) {
        return jcallback_BrokerError(errorMsg);
    }

    public int callProgress(final int progress) {
        return jcallback_BrokerProgress(progress);
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
