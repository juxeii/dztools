package com.jforex.dzjforex.brokertime.ntp;

import java.net.InetAddress;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.NtpV3Packet;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.commons.net.ntp.TimeStamp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jforex.dzjforex.config.PluginConfig;

import io.reactivex.Single;

public class NTPFetch {

    private final Single<Long> fetch;
    private final NTPUDPClient ntpUDPClient;

    private final static Logger logger = LogManager.getLogger(NTPFetch.class);

    public NTPFetch(final NTPUDPClient ntpUDPClient,
                    final PluginConfig pluginConfig) {
        this.ntpUDPClient = ntpUDPClient;

        fetch = fromURL(pluginConfig.ntpServerURL());
    }

    private Single<Long> fromURL(final String ntpServerURL) {
        return Single
            .fromCallable(() -> InetAddress.getByName(ntpServerURL))
            .doOnSubscribe(d -> logger.debug("Fetching NTP from " + ntpServerURL))
            .map(ntpUDPClient::getTime)
            .map(TimeInfo::getMessage)
            .map(NtpV3Packet::getTransmitTimeStamp)
            .map(TimeStamp::getTime)
            .doOnError(e -> logger.error("NTP fetch task failed with error: " + e.getMessage()));
    }

    public Single<Long> get() {
        return fetch;
    }
}
