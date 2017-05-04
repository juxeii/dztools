package com.jforex.dzjforex.time;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.NtpV3Packet;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.commons.net.ntp.TimeStamp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jforex.dzjforex.config.PluginConfig;

import io.reactivex.Flowable;
import io.reactivex.Single;

public class NTPFetch {

    private final Single<Long> observable;
    private final NTPUDPClient ntpUDPClient = new NTPUDPClient();

    private final static Logger logger = LogManager.getLogger(NTPFetch.class);

    public NTPFetch(final PluginConfig pluginConfig) {
        final long retryDelay = pluginConfig.ntpRetryDelay();

        observable = Single
            .fromCallable(() -> fromURL(pluginConfig.ntpServerURL()))
            .doOnSubscribe(d -> logger.debug("Fetching NTP now..."))
            .doOnError(err -> logger.debug("NTP fetch task failed with error: " + err.getMessage()
                    + "! Will retry in " + retryDelay + " milliseconds."))
            .retryWhen(errors -> errors.flatMap(error -> Flowable.timer(retryDelay, TimeUnit.MILLISECONDS)));
    }

    private long fromURL(final String ntpServerURL) throws Exception {
        final InetAddress inetAddress = InetAddress.getByName(ntpServerURL);
        final TimeInfo timeInfo = ntpUDPClient.getTime(inetAddress);
        final NtpV3Packet ntpV3Packet = timeInfo.getMessage();
        final TimeStamp timeStamp = ntpV3Packet.getTransmitTimeStamp();
        return timeStamp.getTime();
    }

    public Single<Long> observable() {
        return observable;
    }
}
