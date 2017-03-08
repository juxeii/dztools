package com.jforex.dzjforex.brokerapi;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.ICurrency;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.system.IClient;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.misc.AccountInfo;
import com.jforex.programming.currency.CurrencyFactory;
import com.jforex.programming.instrument.InstrumentFactory;

public class BrokerSubscribe {

    private final IClient client;
    private final AccountInfo accountInfo;
    private final Set<Instrument> instrumentsToSubscribe = new HashSet<Instrument>();

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
        final Optional<Instrument> maybeInstrument = InstrumentFactory.maybeFromName(instrumentName);
        return maybeInstrument.isPresent()
                ? subscribeValidInstrumentName(maybeInstrument.get())
                : ZorroReturnValues.ASSET_UNAVAILABLE.getValue();
    }

    private int subscribeValidInstrumentName(final Instrument instrumentToSubscribe) {
        if (isInstrumentSubscribed(instrumentToSubscribe)) {
            logger.warn("Instrument " + instrumentToSubscribe + " already subscribed.");
            return ZorroReturnValues.ASSET_AVAILABLE.getValue();
        }

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
        final Set<Instrument> crossInstruments = crossInstruments(instrumentToSubscribe);
        instrumentsToSubscribe.add(instrumentToSubscribe);
        instrumentsToSubscribe.addAll(crossInstruments);

        logger.debug("Trying to subscribe instrument " + instrumentToSubscribe +
                " with cross instruments " + crossInstruments);
        client.setSubscribedInstruments(instrumentsToSubscribe);
        logger.debug("Subscribed instruments are:" + client.getSubscribedInstruments());
    }
}
