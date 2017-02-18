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

import io.reactivex.Observable;

public class NTPFetch {

    private final Observable<Long> observable;

    private final static Logger logger = LogManager.getLogger(NTPFetch.class);

    public NTPFetch(final PluginConfig pluginConfig) {
        final NTPUDPClient ntpUDPClient = new NTPUDPClient();
        final long retryDelay = pluginConfig.NTP_RETRY_DELAY();

        observable = Observable
            .fromCallable(() -> fetchFromURL(ntpUDPClient, pluginConfig.NTP_TIME_SERVER_URL()))
            .doOnSubscribe(d -> logger.debug("Fetching NTP now..."))
            .doOnError(err -> logger.debug("NTP fetch task failed with error: " + err.getMessage()
                    + ". Will retry in " + retryDelay + " milliseconds."))
            .retryWhen(errors -> errors.flatMap(error -> Observable.timer(retryDelay, TimeUnit.MILLISECONDS)));
    }

    private long fetchFromURL(final NTPUDPClient ntpUDPClient,
                              final String ntpServerURL) throws Exception {
        final InetAddress inetAddress = InetAddress.getByName(ntpServerURL);
        final TimeInfo timeInfo = ntpUDPClient.getTime(inetAddress);
        final NtpV3Packet ntpV3Packet = timeInfo.getMessage();
        final TimeStamp timeStamp = ntpV3Packet.getTransmitTimeStamp();
        return timeStamp.getTime();
    }

    public Observable<Long> observable() {
        return observable;
    }
}
