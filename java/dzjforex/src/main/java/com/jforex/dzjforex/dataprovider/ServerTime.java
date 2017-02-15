package com.jforex.dzjforex.dataprovider;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.ITick;
import com.jforex.dzjforex.misc.NTPTimeSynchTask;
import com.jforex.dzjforex.misc.StrategyForData;
import com.jforex.dzjforex.settings.PluginConfig;

public class ServerTime {

    private final StrategyForData strategy;
    private final NTPTimeSynchTask ntpSynchTask;
    private SynchState snychState;
    private long serverSynchTimer;
    private Future<Long> ntpFuture;
    private final ExecutorService singleThreadExecutor;
    private long startNTPTime;
    private long ntpTimer;
    private final PluginConfig pluginConfig = ConfigFactory.create(PluginConfig.class);

    private final static Logger logger = LogManager.getLogger(ServerTime.class);

    private enum SynchState {
        NTP,
        TICK
    }

    public ServerTime(final StrategyForData strategy) {
        this.strategy = strategy;
        ntpSynchTask = new NTPTimeSynchTask();
        singleThreadExecutor = Executors.newSingleThreadExecutor();

        init();
    }

    private void init() {
        snychState = SynchState.TICK;
        serverSynchTimer = System.currentTimeMillis();
        startNTPSynch();
        // at init wait for result
        getNTPFuture();
    }

    private void startNTPSynch() {
        logger.debug("Starting ntpSynchTask...");
        ntpFuture = singleThreadExecutor.submit(ntpSynchTask);
    }

    public long get() {
        if (snychState == SynchState.NTP)
            return startNTPTime + (System.currentTimeMillis() - ntpTimer);
        // We are using tick based server time here
        // Check if synch is ongoing
        if (ntpFuture.isDone()) {
            logger.debug("ntpSynchTask result available.");
            startNTPTime = getNTPFuture();
            if (startNTPTime != 0L) {
                // NTP time now available
                snychState = SynchState.NTP;
                ntpTimer = System.currentTimeMillis();
                logger.debug("Switched to SynchState NTP");
                return startNTPTime;
            } else {
                logger.debug("Synch taks failed, reset synch timer");
                serverSynchTimer = System.currentTimeMillis();
            }
        }
        // No synch ongoing, check if synch is triggered
        if (isServerTimeSynchTriggered()) {
            logger.debug("Server time synching was triggered.");
            startNTPSynch();
            serverSynchTimer = System.currentTimeMillis();
        }
        final long latestTickTime = getLatestTickTime();
        if (latestTickTime == 0L) {
            logger.warn("latestTickTime is invalid, server time currently not available, fallback to system time!");
            return System.currentTimeMillis();
        }
        return latestTickTime;
    }

    private long getNTPFuture() {
        try {
            return ntpFuture.get();
        } catch (final InterruptedException e) {
            logger.error("InterruptedException: " + e.getMessage());
        } catch (final ExecutionException e) {
            logger.error("ExecutionException: " + e.getMessage());
        }
        return 0L;
    }

    private long getLatestTickTime() {
        final ITick tick = strategy.getLatestTick();
        if (tick == null)
            return 0L;
        return tick.getTime();
    }

    private boolean isServerTimeSynchTriggered() {
        if ((System.currentTimeMillis() - serverSynchTimer) >= pluginConfig.SERVERTIME_SYNC_MILLIS())
            return true;
        return false;
    }
}
