package com.jforex.dzjforex.datetime;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;

import org.aeonbits.owner.ConfigFactory;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jforex.dzjforex.config.PluginConfig;

public class NTPTimeSynchTask implements Callable<Long> {

    private final NTPUDPClient timeClient;
    private InetAddress inetAddress;
    private TimeInfo timeInfo;
    private final PluginConfig pluginConfig = ConfigFactory.create(PluginConfig.class);

    private final static Logger logger = LogManager.getLogger(NTPTimeSynchTask.class);

    public NTPTimeSynchTask() {
        timeClient = new NTPUDPClient();
        init();
    }

    private void init() {
        timeClient.setDefaultTimeout(pluginConfig.NTP_TIMEOUT());
        try {
            inetAddress = InetAddress.getByName(pluginConfig.NTP_TIME_SERVER_URL());
        } catch (final UnknownHostException e) {
            logger.error("NTP server url " + pluginConfig.NTP_TIME_SERVER_URL() + " is not reachable!");
        }
    }

    @Override
    public Long call() {
        if (inetAddress == null)
            return 0L;
        try {
            timeInfo = timeClient.getTime(inetAddress);
        } catch (final IOException e) {
            logger.warn("Unable to get time from NTP server: " + e.getMessage());
            return 0L;
        }
        final long ntpTime = timeInfo.getMessage().getTransmitTimeStamp().getTime();
        logger.debug("ntpSynchTask successful. Time: " + DateTimeUtils.formatDateTime(ntpTime));
        return ntpTime;
    }
}
