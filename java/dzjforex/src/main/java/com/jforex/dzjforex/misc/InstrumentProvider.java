package com.jforex.dzjforex.misc;

import java.util.HashMap;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.ICurrency;
import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.ZorroLogger;
import com.jforex.programming.instrument.InstrumentFactory;

public class InstrumentProvider {

    private static final HashMap<String, Instrument> instrumentByZorroName = new HashMap<String, Instrument>();
    private final static Logger logger = LogManager.getLogger(InstrumentProvider.class);

    public static Optional<Instrument> fromName(final String instrumentName) {
        if (instrumentByZorroName.containsKey(instrumentName))
            return Optional.of(instrumentByZorroName.get(instrumentName));
        return createNewName(instrumentName);
    }

    public static Optional<Instrument> fromCurrencies(final ICurrency currencyA,
                                                      final ICurrency currencyB) {
        return InstrumentFactory.maybeFromCurrencies(currencyA, currencyB);
    }

    private static Optional<Instrument> createNewName(final String instrumentName) {
        final Optional<Instrument> instrumentOpt = InstrumentFactory.maybeFromName(instrumentName);
        if (!instrumentOpt.isPresent()) {
            logger.warn(instrumentName + " is no valid asset name!");
            ZorroLogger.log(instrumentName + " is no valid asset name!");
            return Optional.empty();
        }
        final Instrument instrument = instrumentOpt.get();
        instrumentByZorroName.put(instrumentName, instrument);
        return Optional.of(instrument);
    }
}
