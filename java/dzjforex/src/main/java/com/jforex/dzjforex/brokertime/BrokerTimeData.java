package com.jforex.dzjforex.brokertime;

public class BrokerTimeData {

    private final double pTimeUTC[];

    public BrokerTimeData(final double pTimeUTC[]) {
        this.pTimeUTC = pTimeUTC;
    }

    public void fill(final double oleTime) {
        pTimeUTC[0] = oleTime;
    }
}
