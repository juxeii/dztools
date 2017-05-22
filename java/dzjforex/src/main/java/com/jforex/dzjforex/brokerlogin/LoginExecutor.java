package com.jforex.dzjforex.brokerlogin;

import com.jforex.programming.connection.Authentification;

import io.reactivex.Completable;
import io.reactivex.Single;

public class LoginExecutor {

    private final Authentification authentification;
    private final CredentialsFactory credentialsFactory;

    public LoginExecutor(final Authentification authentification,
                         final CredentialsFactory credentialsFactory) {
        this.authentification = authentification;
        this.credentialsFactory = credentialsFactory;
    }

    public Completable login(final BrokerLoginData brokerLoginData) {
        return Single
            .fromCallable(() -> credentialsFactory.create(brokerLoginData))
            .flatMapCompletable(authentification::login);
    }

    public Completable logout() {
        return Completable.defer(() -> authentification.logout());
    }
}
