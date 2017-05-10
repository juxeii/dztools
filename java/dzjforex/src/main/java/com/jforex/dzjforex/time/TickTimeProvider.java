package com.jforex.dzjforex.time;

import java.time.Clock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.ITick;
import com.dukascopy.api.JFException;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.programming.quote.TickQuote;
import com.jforex.programming.quote.TickQuoteRepository;

import io.reactivex.Single;

public class TickTimeProvider {

    private final TickQuoteRepository tickQuoteRepository;
    private final Clock clock;
    private long latestTickTime;
    private long synchTime;

    private final static int INVALID_SERVER_TIME = ZorroReturnValues.INVALID_SERVER_TIME.getValue();
    private final static Logger logger = LogManager.getLogger(TickTimeProvider.class);

    public TickTimeProvider(final TickQuoteRepository tickQuoteRepository,
                            final Clock clock) {
        this.tickQuoteRepository = tickQuoteRepository;
        this.clock = clock;
    }

    public Single<Long> get() {
        final long latestTickTimeFromRepository = fromRepository();
        return latestTickTimeFromRepository == INVALID_SERVER_TIME
                ? Single.error(new JFException("Fetching tick time failed!"))
                : Single.just(getForValidTickTime(latestTickTimeFromRepository));
    }

    private long getForValidTickTime(final long latestTickTimeFromRepository) {
        return latestTickTimeFromRepository > latestTickTime
                ? setNewAndSynch(latestTickTimeFromRepository)
                : estimateWithSynchTime();
    }

    private long fromRepository() {
        logger.debug("Fetching tick times from repository...");
        return tickQuoteRepository
            .getAll()
            .values()
            .stream()
            .map(TickQuote::tick)
            .mapToLong(ITick::getTime)
            .max()
            .orElseGet(() -> INVALID_SERVER_TIME);
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
