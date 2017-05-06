package com.jforex.dzjforex.misc;

import java.util.Optional;

import com.dukascopy.api.Instrument;
import com.jforex.programming.instrument.InstrumentFactory;

import io.reactivex.Maybe;

public class RxUtility {

    public static <T> Maybe<T> optionalToMaybe(final Optional<T> maybeT) {
        return maybeT.isPresent()
                ? Maybe.just(maybeT.get())
                : Maybe.empty();
    }

    public static Maybe<Instrument> maybeInstrumentFromName(final String assetName) {
        final Optional<Instrument> maybeInstrument = InstrumentFactory.maybeFromName(assetName);
        return optionalToMaybe(maybeInstrument);
    }
}
