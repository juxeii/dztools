package com.jforex.dzjforex.handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jforex.dzjforex.Zorro;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.misc.CredentialsFactory;
import com.jforex.programming.connection.Authentification;
import com.jforex.programming.connection.LoginCredentials;

import io.reactivex.schedulers.Schedulers;

public class LoginHandler {

    private final Authentification authentification;
    private final CredentialsFactory credentialsFactory;
    private final Zorro zorro;
    private LoginState loginState = LoginState.LOGGED_OUT;

    private final static Logger logger = LogManager.getLogger(LoginHandler.class);

    public LoginHandler(final Authentification authentification,
                        final CredentialsFactory credentialsFactory,
                        final Zorro zorro) {
        this.authentification = authentification;
        this.credentialsFactory = credentialsFactory;
        this.zorro = zorro;
    }

    public int login(final String username,
                     final String password,
                     final String loginType) {
        final LoginCredentials credentials = credentialsFactory.create(username,
                                                                       password,
                                                                       loginType);
        return loginWithCredentials(credentials);
    }

    private int loginWithCredentials(final LoginCredentials credentials) {
        loginState = LoginState.CONNECTING;

        authentification
            .login(credentials)
            .subscribeOn(Schedulers.io())
            .subscribe(() -> loginState = LoginState.LOGGED_IN,
                       err -> {
                           logger.error("Failed to login with exception " + err.getMessage());
                           loginState = LoginState.LOGIN_FAIL;
                       });

        logger.debug("Waiting for login...");
        zorro.progressWait(() -> loginState == LoginState.CONNECTING);

        return loginState == LoginState.LOGGED_IN
                ? ZorroReturnValues.LOGIN_OK.getValue()
                : ZorroReturnValues.LOGIN_FAIL.getValue();
    }

    public int logout() {
        authentification
            .logout()
            .blockingAwait();
        return ZorroReturnValues.LOGOUT_OK.getValue();
    }
}
