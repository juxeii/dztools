package com.jforex.dzjforex.time;

import java.time.Clock;

import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.programming.quote.TickQuoteRepository;

public class TickTimeProvider {

    private final TickQuoteRepository tickQuoteRepository;
    private final Clock clock;
    private long latestTickTime;
    private long synchTime;

    public TickTimeProvider(final TickQuoteRepository tickQuoteRepository,
                            final Clock clock) {
        this.tickQuoteRepository = tickQuoteRepository;
        this.clock = clock;
    }

    public long get() {
        final long latestTickTimeFromRepository = fromRepository();

        return latestTickTimeFromRepository == ZorroReturnValues.INVALID_SERVER_TIME.getValue()
                ? ZorroReturnValues.INVALID_SERVER_TIME.getValue()
                : getForValidTickTime(latestTickTimeFromRepository);
    }

    private long getForValidTickTime(final long latestTickTimeFromRepository) {
        return latestTickTimeFromRepository > latestTickTime
                ? setNewAndSynch(latestTickTimeFromRepository)
                : estimateWithSynchTime();
    }

    private long fromRepository() {
        return tickQuoteRepository
            .getAll()
            .values()
            .stream()
            .map(quote -> quote.tick())
            .mapToLong(tick -> tick.getTime())
            .max()
            .orElseGet(() -> ZorroReturnValues.INVALID_SERVER_TIME.getValue());
    }

    private long setNewAndSynch(final long latestTickTimeFromRepository) {
        latestTickTime = latestTickTimeFromRepository;
        synchTime = clock.millis();
        return latestTickTime;
    }

    private long estimateWithSynchTime() {
        final long timeDiffToSynchTime = clock.millis() - synchTime;
        return latestTickTime + timeDiffToSynchTime;
    }
}