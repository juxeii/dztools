package com.jforex.dzjforex.misc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.system.ClientFactory;
import com.dukascopy.api.system.IClient;

import io.reactivex.Observable;

public class ClientProvider {

    private final static Logger logger = LogManager.getLogger(ClientProvider.class);

    public static IClient get() {
        return Observable
            .fromCallable(() -> ClientFactory.getDefaultInstance())
            .doOnError(err -> logger.error("Error while login! " + err.getMessage()))
            .blockingFirst();
    }
}
