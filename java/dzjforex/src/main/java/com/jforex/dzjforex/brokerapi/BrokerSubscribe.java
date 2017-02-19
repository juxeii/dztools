package com.jforex.dzjforex.brokerapi;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.ICurrency;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.system.IClient;
import com.jforex.dzjforex.config.Constant;
import com.jforex.dzjforex.handler.AccountInfo;
import com.jforex.dzjforex.handler.InstrumentHandler;

public class BrokerSubscribe {

    private final IClient client;
    private final AccountInfo accountInfo;

    private final static Logger logger = LogManager.getLogger(BrokerSubscribe.class);

    public BrokerSubscribe(final IClient client,
                           final AccountInfo accountInfo) {
        this.client = client;
        this.accountInfo = accountInfo;
    }

    public int subscribe(final String instrumentName) {
        final Optional<Instrument> maybeInstrument = InstrumentHandler.fromName(instrumentName);
        return maybeInstrument.isPresent()
                ? subscribeValidinstrumentName(maybeInstrument.get())
                : Constant.ASSET_UNAVAILABLE;
    }

    private int subscribeValidinstrumentName(final Instrument instrumentToSubscribe) {
        final Set<Instrument> instrumentsToSubscribe = new HashSet<Instrument>();
        instrumentsToSubscribe.add(instrumentToSubscribe);
        createCrossInstrumentIfNeeded(instrumentsToSubscribe, instrumentToSubscribe);

        logger.info("Subscribing " + instrumentsToSubscribe + " now.");
        client.setSubscribedInstruments(instrumentsToSubscribe);
        return Constant.ASSET_AVAILABLE;
    }

    private void createCrossInstrumentIfNeeded(final Set<Instrument> instrumentsToSubscribe,
                                               final Instrument toSubscribeInstrument) {
        final ICurrency accountCurrency = accountInfo.currency();
        final ICurrency crossCurrency = toSubscribeInstrument.getPrimaryJFCurrency();

        if (crossCurrency != accountCurrency) {
            final Instrument crossInstrument = InstrumentHandler
                .fromCurrencies(accountCurrency, crossCurrency)
                .get();
            instrumentsToSubscribe.add(crossInstrument);
            logger.debug("Created cross instrument " + crossInstrument + " for subscription: \n"
                    + " accountCurrency: " + accountCurrency + "\n"
                    + " crossCurrency: " + crossCurrency);
        }
    }
}
