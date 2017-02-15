package com.jforex.dzjforex.handler;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.Instrument;
import com.dukascopy.api.system.IClient;
import com.jforex.dzjforex.ZorroLogger;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.dzjforex.config.ReturnCodes;
import com.jforex.dzjforex.misc.InstrumentProvider;

public class SubscriptionHandler {

    private final IClient client;
    private final AccountHandler accountHandler;
    private final PluginConfig pluginConfig = ConfigFactory.create(PluginConfig.class);

    private final static Logger logger = LogManager.getLogger(SubscriptionHandler.class);

    public SubscriptionHandler(final IClient client,
                               final AccountHandler accountHandler) {
        this.client = client;
        this.accountHandler = accountHandler;
    }

    public int doSubscribeAsset(final String instrumentName) {
        final Optional<Instrument> toSubscribeInstrumentOpt = InstrumentProvider.fromName(instrumentName);
        if (!toSubscribeInstrumentOpt.isPresent())
            return ReturnCodes.ASSET_UNAVAILABLE;

        final Instrument toSubscribeInstrument = toSubscribeInstrumentOpt.get();
        final Set<Instrument> instruments = new HashSet<Instrument>();
        instruments.add(toSubscribeInstrument);
        // we must subscribe to cross instrument also for margin calculations
        final Optional<Instrument> crossInstrumentOpt = InstrumentProvider
            .fromCurrencies(accountHandler.getCurrency(), toSubscribeInstrument.getPrimaryJFCurrency());
        final Instrument crossInstrument = crossInstrumentOpt.get();
        if (crossInstrument != null) {
            logger.debug("crossInstrument: " + crossInstrument);
            instruments.add(crossInstrument);
        }

        final int subscriptionResult = subscribe(instruments);
        logger.info("Subscription for " + toSubscribeInstrument + " successful.");
        return subscriptionResult;
    }

    private int subscribe(final Set<Instrument> instruments) {
        client.setSubscribedInstruments(instruments);

        waitForSubscription(instruments);
        if (!client.getSubscribedInstruments().containsAll(instruments)) {
            ZorroLogger.showError("Subscription for assets failed!");
            return ReturnCodes.ASSET_UNAVAILABLE;
        }
        return ReturnCodes.ASSET_AVAILABLE;
    }

    private void waitForSubscription(final Set<Instrument> instruments) {
        for (int i = 0; i < pluginConfig.SUBSCRIPTION_WAIT_TIME_RETRIES(); ++i) {
            if (client.getSubscribedInstruments().containsAll(instruments))
                break;
            try {
                Thread.sleep(pluginConfig.SUBSCRIPTION_WAIT_TIME());
            } catch (final InterruptedException e) {
                logger.error("Thread exc: " + e.getMessage());
                ZorroLogger.indicateError();
                break;
            }
        }
    }
}
