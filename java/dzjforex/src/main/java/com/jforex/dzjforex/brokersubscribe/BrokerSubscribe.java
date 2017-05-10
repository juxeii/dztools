package com.jforex.dzjforex.brokersubscribe;

import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.ICurrency;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.system.IClient;
import com.google.common.collect.Sets;
import com.jforex.dzjforex.brokeraccount.AccountInfo;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.misc.RxUtility;
import com.jforex.programming.currency.CurrencyFactory;
import com.jforex.programming.currency.CurrencyUtil;
import com.jforex.programming.instrument.InstrumentFactory;

public class BrokerSubscribe {

    private final IClient client;
    private final AccountInfo accountInfo;

    private final static Logger logger = LogManager.getLogger(BrokerSubscribe.class);

    public BrokerSubscribe(final IClient client,
                           final AccountInfo accountInfo) {
        this.client = client;
        this.accountInfo = accountInfo;
    }

    public Set<Instrument> subscribedInstruments() {
        return client.getSubscribedInstruments();
    }

    public int subscribe(final String instrumentName) {
        return RxUtility
            .maybeInstrumentFromName(instrumentName)
            .map(this::subscribeValidInstrumentName)
            .defaultIfEmpty(ZorroReturnValues.ASSET_UNAVAILABLE.getValue())
            .blockingGet();
    }

    private int subscribeValidInstrumentName(final Instrument instrumentToSubscribe) {
        if (!isInstrumentSubscribed(instrumentToSubscribe))
            subscribeWithCrossInstruments(instrumentToSubscribe);
        return ZorroReturnValues.ASSET_AVAILABLE.getValue();
    }

    private boolean isInstrumentSubscribed(final Instrument instrument) {
        return client
            .getSubscribedInstruments()
            .contains(instrument);
    }

    private Set<Instrument> crossInstruments(final Instrument instrumentToSubscribe) {
        final Set<ICurrency> crossCurrencies = CurrencyFactory.fromInstrument(instrumentToSubscribe);
        return InstrumentFactory.combineWithAnchorCurrency(accountInfo.currency(), crossCurrencies);
    }

    private void subscribeWithCrossInstruments(final Instrument instrumentToSubscribe) {
        final Set<Instrument> instrumentsToSubscribe = Sets.newHashSet(instrumentToSubscribe);
        if (!CurrencyUtil.isInInstrument(accountInfo.currency(), instrumentToSubscribe))
            instrumentsToSubscribe.addAll(crossInstruments(instrumentToSubscribe));

        logger.debug("Trying to subscribe instruments " + instrumentsToSubscribe);
        client.setSubscribedInstruments(instrumentsToSubscribe);
        logger.debug("Subscribed instruments:" + client.getSubscribedInstruments());
    }
}
