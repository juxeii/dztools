package com.jforex.dzjforex.brokerlogin;

import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jforex.dzjforex.config.PluginConfig;

import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;

public class LoginRetryTimer {

    private final Completable delayTimer;
    private final BehaviorSubject<Boolean> isLoginPermitted = BehaviorSubject.createDefault(true);

    private final static Logger logger = LogManager.getLogger(LoginRetryTimer.class);

    public LoginRetryTimer(final PluginConfig pluginConfig) {
        delayTimer = Completable
            .timer(pluginConfig.loginRetryDelay(),
                   TimeUnit.MILLISECONDS,
                   Schedulers.io())
            .doOnSubscribe(d -> {
                isLoginPermitted.onNext(false);
                logger.debug("Starting login retry delay timer. Login is not available until timer elapsed.");
            })
            .doOnComplete(() -> logger.debug("Login retry delay timer completed. Login is available again."));
    }

    public void start() {
        delayTimer.subscribe(() -> isLoginPermitted.onNext(true));
    }

    public boolean isLoginPermitted() {
        return isLoginPermitted.getValue();
    }
}
