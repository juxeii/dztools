package com.jforex.dzjforex.misc.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IDataService;
import com.dukascopy.api.ITimeDomain;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Period;
import com.jforex.dzjforex.misc.MarketData;
import com.jforex.dzjforex.test.util.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;

@RunWith(HierarchicalContextRunner.class)
public class MarketDataTest extends CommonUtilForTest {

    private MarketData marketData;

    @Mock
    private IDataService dataServiceMock;
    @Mock
    private ITimeDomain timeDomainMock;

    private static final long currentServerTime = 12L;
    private static final long lookUpEndTime = currentServerTime + Period.ONE_MIN.getInterval();

    @Before
    public void setUp() {
        marketData = new MarketData(dataServiceMock);
    }

    @Test
    public void whenGetOfflineTimeDomainsThrowsTheMarketIsClosed() throws JFException {
        when(dataServiceMock.getOfflineTimeDomains(currentServerTime, lookUpEndTime))
            .thenThrow(jfException);

        assertTrue(marketData.isMarketOffline(currentServerTime));
    }

    public class WhenOfflineTimeDomainsAreAvailable {

        private final Set<ITimeDomain> offlineDomains = new HashSet<>();

        private void setOfflineDomain(final long domainStartTime,
                                      final long domainEndTime) {
            when(timeDomainMock.getStart()).thenReturn(domainStartTime);
            when(timeDomainMock.getEnd()).thenReturn(domainEndTime);
        }

        @Before
        public void setUp() throws JFException {
            offlineDomains.add(timeDomainMock);

            when(dataServiceMock.getOfflineTimeDomains(currentServerTime, lookUpEndTime))
                .thenReturn(offlineDomains);
        }

        public class ServerTimeIsInOfflineDomain {

            @Before
            public void setUp() {
                setOfflineDomain(9L, 14L);
            }

            @Test
            public void marketIsOffline() {
                assertTrue(marketData.isMarketOffline(currentServerTime));
            }
        }

        public class ServerTimeIsNotInOfflineDomain {

            @Before
            public void setUp() {
                setOfflineDomain(13L, 14L);
            }

            @Test
            public void marketIsOnline() {
                assertFalse(marketData.isMarketOffline(currentServerTime));
            }
        }
    }
}
