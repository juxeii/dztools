package com.jforex.dzjforex.brokertime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.ITick;
import com.dukascopy.api.JFException;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.programming.misc.DateTimeUtil;
import com.jforex.programming.quote.TickQuote;
import com.jforex.programming.quote.TickQuoteRepository;

import io.reactivex.Single;

public class TickTimeProvider {

    private final TickQuoteRepository tickQuoteRepository;
    private final TimeWatch timeWatch;

    private final static int INVALID_SERVER_TIME = ZorroReturnValues.INVALID_SERVER_TIME.getValue();
    private final static Logger logger = LogManager.getLogger(TickTimeProvider.class);

    public TickTimeProvider(final TickQuoteRepository tickQuoteRepository,
                            final TimeWatch timeWatch) {
        this.tickQuoteRepository = tickQuoteRepository;
        this.timeWatch = timeWatch;
    }

    public Single<Long> get() {
        return Single
            .fromCallable(this::fromRepository)
            .doOnSuccess(latestTickTime -> logger.debug("Fetched latest tick time "
                    + DateTimeUtil.formatMillis(latestTickTime)))
            .flatMap(latestTickTime -> latestTickTime == INVALID_SERVER_TIME
                    ? Single.error(new JFException("Fetching tick time failed!"))
                    : Single.just(getForValidTickTime(latestTickTime)));
    }

    private long getForValidTickTime(final long latestTickTime) {
        final long currentTimeWatch = timeWatch.get();
        return latestTickTime > currentTimeWatch
                ? synchTickTime(latestTickTime)
                : currentTimeWatch;
    }

    private long fromRepository() {
        return tickQuoteRepository
            .getAll()
            .values()
            .stream()
            .map(TickQuote::tick)
            .mapToLong(ITick::getTime)
            .max()
            .orElseGet(() -> INVALID_SERVER_TIME);
    }

    private long synchTickTime(final long latestTickTime) {
        timeWatch.synch(latestTickTime);
        return latestTickTime;
    }
}
