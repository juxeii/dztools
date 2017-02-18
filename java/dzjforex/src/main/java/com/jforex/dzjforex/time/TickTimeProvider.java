package com.jforex.dzjforex.time;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jforex.programming.misc.DateTimeUtil;
import com.jforex.programming.quote.TickQuoteRepository;

public class TickTimeProvider {

    private final TickQuoteRepository tickQuoteRepository;
    private long latestTickTime;
    private long synchTime;

    private final static Logger logger = LogManager.getLogger(TickTimeProvider.class);

    public TickTimeProvider(final TickQuoteRepository tickQuoteRepository) {
        this.tickQuoteRepository = tickQuoteRepository;
    }

    public long get() {
        final long latestTimeFromRepository = fromRepository();
        logger.info("latestTimeFromRepository is " + DateTimeUtil.formatMillis(latestTimeFromRepository));

        if (latestTimeFromRepository > latestTickTime) {
            latestTickTime = latestTimeFromRepository;
            synchTime = DateTimeUtil.localMillisNow();
            logger.info("new latest tick available with " + DateTimeUtil.formatMillis(latestTickTime));
            return latestTickTime;
        } else
            return estimateWithSynchTime();
    }

    private long fromRepository() {
        final long test = tickQuoteRepository
            .getAll()
            .values()
            .stream()
            .mapToLong(quote -> {
                logger.info("quote for " + quote.instrument() + " is " + quote.tick().getTime());
                return quote.tick().getTime();
            })
            .max()
            .orElseGet(() -> 0L);
        return test;
    }

    private long estimateWithSynchTime() {
        final long timeDiffToSynchTime = DateTimeUtil.localMillisNow() - synchTime;
        logger.info("estimateWithSynchTime called with synchTime "
                + DateTimeUtil.formatMillis(synchTime)
                + " and latestTickTime " + DateTimeUtil.formatMillis(synchTime));
        return latestTickTime + timeDiffToSynchTime;
    }
}
