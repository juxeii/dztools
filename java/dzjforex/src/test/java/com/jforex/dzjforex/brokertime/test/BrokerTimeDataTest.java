package com.jforex.dzjforex.brokertime.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.jforex.dzjforex.brokertime.BrokerTimeData;
import com.jforex.dzjforex.misc.TimeConvert;
import com.jforex.dzjforex.testutil.CommonUtilForTest;

public class BrokerTimeDataTest extends CommonUtilForTest {

    private BrokerTimeData brokerTimeData;

    private final double pTimeUTC[] = new double[1];
    private final long serverTime = 42L;

    @Before
    public void setUp() {
        brokerTimeData = new BrokerTimeData(pTimeUTC);

        brokerTimeData.fill(serverTime);
    }

    @Test
    public void serverTimeIsCorrectFilledWithUTCTime() {
        assertThat(pTimeUTC[0], equalTo(TimeConvert.getOLEDateFromMillis(serverTime)));
    }
}
