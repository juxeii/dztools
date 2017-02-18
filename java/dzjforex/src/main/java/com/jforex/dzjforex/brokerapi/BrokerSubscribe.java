package com.jforex.dzjforex.brokerapi;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.Instrument;
import com.dukascopy.api.system.IClient;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.dzjforex.config.Constant;
import com.jforex.dzjforex.handler.AccountInfo;
import com.jforex.dzjforex.handler.InstrumentHandler;

import io.reactivex.Observable;

public class BrokerSubscribe {

    private final IClient client;
    private final AccountInfo accountInfo;
    private final PluginConfig pluginConfig = ConfigFactory.create(PluginConfig.class);

    private final static Logger logger = LogManager.getLogger(BrokerSubscribe.class);

    public BrokerSubscribe(final IClient client,
                           final AccountInfo accountInfo) {
        this.client = client;
        this.accountInfo = accountInfo;
    }

    public int handle(final String instrumentName) {
        return InstrumentHandler.executeForInstrument(instrumentName,
                                                      this::handleForValidInstrument,
                                                      Constant.ASSET_UNAVAILABLE);
    }

    private int handleForValidInstrument(final Instrument toSubscribeInstrument) {
        final Set<Instrument> instruments = new HashSet<Instrument>();
        instruments.add(toSubscribeInstrument);
        if (toSubscribeInstrument.getPrimaryJFCurrency() != accountInfo.currency()) {
            // we must subscribe to cross instrument also for margin
            // calculations
            final Instrument instrument = InstrumentHandler
                .fromCurrencies(accountInfo.currency(), toSubscribeInstrument.getPrimaryJFCurrency())
                .get();
            instruments.add(instrument);
        }

        return subscribe(instruments);
    }

    private int subscribe(final Set<Instrument> instruments) {
        client.setSubscribedInstruments(instruments);

        return Observable
            .interval(pluginConfig.SUBSCRIPTION_WAIT_TIME(), TimeUnit.MILLISECONDS)
            .take(pluginConfig.SUBSCRIPTION_WAIT_TIME_RETRIES())
            .takeUntil(att -> (Boolean) allInstrumentsSubscribed(instruments))
            .map(waitAttempt -> {
                if (allInstrumentsSubscribed(instruments)) {
                    logger.info("Subscription for " + instruments + " done.");
                    return Constant.ASSET_AVAILABLE;
                }
                logger.error("Subscription for " + instruments + " failed!");
                return Constant.ASSET_AVAILABLE;
            })
            .blockingLast();
    }

    private boolean allInstrumentsSubscribed(final Set<Instrument> instruments) {
        return client
            .getSubscribedInstruments()
            .containsAll(instruments);
    }
}
