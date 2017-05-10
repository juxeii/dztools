package com.jforex.dzjforex.brokertime;

import com.jforex.dzjforex.time.TimeConvert;

public class BrokerTimeData {

    private final double pTimeUTC[];

    public BrokerTimeData(final double pTimeUTC[]) {
        this.pTimeUTC = pTimeUTC;
    }

    public void fill(final long serverTime) {
        pTimeUTC[0] = TimeConvert.getOLEDateFromMillis(serverTime);
    }
}
