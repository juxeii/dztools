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
import io.reactivex.subjects.BehaviorSubject;

public class TickTimeProvider {

    private final TickQuoteRepository tickQuoteRepository;
    private final Clock clock;
    private final BehaviorSubject<Long> latestTickTime = BehaviorSubject.create();
    private final BehaviorSubject<Long> synchTime = BehaviorSubject.create();

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
        return latestTickTimeFromRepository > latestTickTime.getValue()
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
        latestTickTime.onNext(latestTickTimeFromRepository);
        synchTime.onNext(clock.millis());
        return latestTickTime.getValue();
    }

    private long estimateWithSynchTime() {
        final long timeDiffToSynchTime = clock.millis() - synchTime.getValue();
        return latestTickTime.getValue() + timeDiffToSynchTime;
    }
}
