package com.jforex.dzjforex.brokersubscribe;

import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.Instrument;
import com.dukascopy.api.system.IClient;
import com.google.common.collect.Sets;

import io.reactivex.Completable;

public class Subscription {

    private final IClient client;

    private final static Logger logger = LogManager.getLogger(Subscription.class);

    public Subscription(final IClient client) {
        this.client = client;
    }

    public Completable set(final List<Instrument> instrumentsToSubscribe) {
        return Completable
            .fromAction(() -> client.setSubscribedInstruments(Sets.newHashSet(instrumentsToSubscribe)))
            .doOnSubscribe(d -> logger.debug("Subscribing instruments: " + instrumentsToSubscribe))
            .doOnComplete(() -> logger.debug("Subscribed instruments:" + instruments()));
    }

    public Set<Instrument> instruments() {
        return client.getSubscribedInstruments();
    }

    public boolean isSubscribed(final Instrument instrument) {
        return instruments().contains(instrument);
    }
}
