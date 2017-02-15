package com.jforex.dzjforex.misc;

import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.ICurrency;
import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.ZorroLogger;

public class InstrumentUtils {

    private static final HashMap<String, Instrument> assetNameToInstrumentMap = new HashMap<String, Instrument>();

    private final static Logger logger = LogManager.getLogger(InstrumentUtils.class);

    public static Instrument getfromString(final String instrumentString) {
        final String uppercaseInstrumentString = instrumentString.toUpperCase();
        if (Instrument.isInverted(uppercaseInstrumentString))
            return Instrument.fromInvertedString(uppercaseInstrumentString);
        return Instrument.fromString(uppercaseInstrumentString);
    }

    public static Instrument getfromCurrencies(final ICurrency currencyA,
                                               final ICurrency currencyB) {
        return getfromString(currencyA + Instrument.getPairsSeparator() + currencyB);
    }

    public static Instrument getByName(final String instrumentName) {
        if (assetNameToInstrumentMap.containsKey(instrumentName))
            return assetNameToInstrumentMap.get(instrumentName);

        return getFromNewName(instrumentName);
    }

    public static String getNameWODash(final Instrument instrument) {
        return instrument.getPrimaryJFCurrency().getCurrencyCode()
                + instrument.getSecondaryJFCurrency().getCurrencyCode();
    }

    private static synchronized Instrument getFromNewName(final String instrumentName) {
        final Instrument instrument = getfromString(instrumentName);
        if (instrument == null) {
            logger.error(instrumentName + " is no valid asset name!");
            ZorroLogger.log(instrumentName + " is no valid asset name!");
            return null;
        }

        assetNameToInstrumentMap.put(instrumentName, instrument);
        return instrument;
    }
}
