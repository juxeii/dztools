package com.jforex.dzjforex.handler;

import java.util.HashMap;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.ICurrency;
import com.dukascopy.api.Instrument;
import com.jforex.programming.instrument.InstrumentFactory;

public class InstrumentHandler {

    private static final HashMap<String, Instrument> instrumentByZorroName = new HashMap<String, Instrument>();
    private final static Logger logger = LogManager.getLogger(InstrumentHandler.class);

    public static Optional<Instrument> fromName(final String assetName) {
        return instrumentByZorroName.containsKey(assetName)
                ? Optional.of(instrumentByZorroName.get(assetName))
                : createNewName(assetName);
    }

    public static Optional<Instrument> fromCurrencies(final ICurrency currencyA,
                                                      final ICurrency currencyB) {
        return InstrumentFactory.maybeFromCurrencies(currencyA, currencyB);
    }

    private static Optional<Instrument> createNewName(final String assetName) {
        final Optional<Instrument> instrumentOpt = InstrumentFactory.maybeFromName(assetName);
        if (!instrumentOpt.isPresent()) {
            logger.error("Invalid asset name " + assetName + " provided!");
            return Optional.empty();
        }
        instrumentByZorroName.put(assetName, instrumentOpt.get());
        return instrumentOpt;
    }
}
