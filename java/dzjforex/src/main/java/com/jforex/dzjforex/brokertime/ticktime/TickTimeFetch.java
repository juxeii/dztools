package com.jforex.dzjforex.brokertime.ticktime;

import java.util.OptionalLong;

import com.dukascopy.api.ITick;
import com.dukascopy.api.JFException;
import com.jforex.programming.quote.TickQuote;
import com.jforex.programming.quote.TickQuoteRepository;

import io.reactivex.Single;

public class TickTimeFetch {

    private final TickQuoteRepository tickQuoteRepository;

    public TickTimeFetch(final TickQuoteRepository tickQuoteRepository) {
        this.tickQuoteRepository = tickQuoteRepository;
    }

    public Single<Long> get() {
        return Single.defer(this::fromRepository);
    }

    private Single<Long> fromRepository() {
        final OptionalLong mayBeTickTime = maybeMaxTickTime();
        return mayBeTickTime.isPresent()
                ? Single.just(mayBeTickTime.getAsLong())
                : Single.error(new JFException("No tick time available!"));
    }

    private OptionalLong maybeMaxTickTime() {
        return tickQuoteRepository
            .getAll()
            .values()
            .stream()
            .map(TickQuote::tick)
            .mapToLong(ITick::getTime)
            .max();
    }
}
