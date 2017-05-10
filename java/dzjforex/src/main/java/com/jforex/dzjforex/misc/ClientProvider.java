package com.jforex.dzjforex.misc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.system.ClientFactory;
import com.dukascopy.api.system.IClient;

import io.reactivex.Single;

public class ClientProvider {

    private final static Logger logger = LogManager.getLogger(ClientProvider.class);

    public static IClient get() {
        return Single
            .fromCallable(() -> ClientFactory.getDefaultInstance())
            .doOnError(err -> logger.error("Error retrieving IClient instance! " + err.getMessage()))
            .blockingGet();
    }
}
