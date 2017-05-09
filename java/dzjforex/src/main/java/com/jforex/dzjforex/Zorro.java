package com.jforex.dzjforex;

import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.dzjforex.misc.HeartBeat;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

public class Zorro {

    private final PluginConfig pluginConfig;
    private Object taskResult;
    private final Observable<HeartBeat<Void>> heartBeatObs;

    private final static int running = 10;
    private final static int done = 0;
    private final static int heartBeatIndication = 1;
    private final static Logger logger = LogManager.getLogger(Zorro.class);

    public Zorro(final PluginConfig pluginConfig) {
        this.pluginConfig = pluginConfig;

        final HeartBeat<Void> hBeat = new HeartBeat<Void>(running, null);
        heartBeatObs = Observable
            .interval(0L,
                      pluginConfig.zorroProgressInterval(),
                      TimeUnit.MILLISECONDS)
            .map(i -> hBeat);
    }

    @SuppressWarnings("unchecked")
    public <T> T progressWait(final Single<T> task) {
        final Observable<HeartBeat<T>> taskObs =
                task.flatMapObservable(taskData -> Observable.just(new HeartBeat<>(done, taskData)));

        Observable
            .merge(heartBeatObs, taskObs)
            .subscribeOn(Schedulers.io())
            .takeUntil(hb -> hb.index() != running)
            .blockingSubscribe(hb -> {
                if (hb.index() != running)
                    taskResult = hb.data();
                else
                    callProgress(heartBeatIndication);
            });

        return (T) taskResult;
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
