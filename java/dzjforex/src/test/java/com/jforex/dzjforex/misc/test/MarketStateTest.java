package com.jforex.dzjforex.misc.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IDataService;
import com.dukascopy.api.ITimeDomain;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Period;
import com.google.common.collect.Sets;
import com.jforex.dzjforex.misc.MarketState;
import com.jforex.dzjforex.test.util.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;

@RunWith(HierarchicalContextRunner.class)
public class MarketStateTest extends CommonUtilForTest {

    private MarketState marketState;

    @Mock
    private IDataService dataServiceMock;
    @Mock
    private ITimeDomain timeDomainMock;

    private static final long currentServerTime = 12L;
    private static final long lookUpEndTime = currentServerTime + Period.ONE_MIN.getInterval();

    @Before
    public void setUp() {
        marketState = new MarketState(dataServiceMock);
    }

    private void assertIsClosed(final boolean expectedState) {
        assertThat(marketState.isClosed(currentServerTime), equalTo(expectedState));
    }

    @Test
    public void whenGetOfflineTimeDomainsThrowsTheMarketIsClosed() throws JFException {
        when(dataServiceMock.getOfflineTimeDomains(currentServerTime, lookUpEndTime))
            .thenThrow(jfException);

        assertIsClosed(true);
    }

    public class WhenOfflineTimeDomainsAreAvailable {

        private final Set<ITimeDomain> offlineDomains = Sets.newHashSet();

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

        @Test
        public void whenServerTimeIsInOfflineDomainMarketIsClosed() {
            setOfflineDomain(9L, 14L);

            assertIsClosed(true);
        }

        @Test
        public void whenServerTimeIsNotInOfflineDomainMarketIsOpen() {
            setOfflineDomain(13L, 14L);

            assertIsClosed(false);
        }
    }
}
