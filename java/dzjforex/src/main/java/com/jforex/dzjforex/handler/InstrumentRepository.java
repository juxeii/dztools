package com.jforex.dzjforex.handler;

import java.util.HashMap;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.Instrument;
import com.jforex.programming.instrument.InstrumentFactory;

public class InstrumentRepository {

    private final static HashMap<String, Instrument> instrumentByAssetName = new HashMap<String, Instrument>();
    private final static Logger logger = LogManager.getLogger(InstrumentRepository.class);

    public static Optional<Instrument> maybeFromName(final String assetName) {
        return instrumentByAssetName.containsKey(assetName)
                ? Optional.of(instrumentByAssetName.get(assetName))
                : createNewName(assetName);
    }

    private static Optional<Instrument> createNewName(final String assetName) {
        final Optional<Instrument> instrumentOpt = InstrumentFactory.maybeFromName(assetName);
        if (!instrumentOpt.isPresent()) {
            logger.error("Invalid asset name " + assetName + " provided!");
            return Optional.empty();
        }
        instrumentByAssetName.put(assetName, instrumentOpt.get());
        return instrumentOpt;
    }
}
