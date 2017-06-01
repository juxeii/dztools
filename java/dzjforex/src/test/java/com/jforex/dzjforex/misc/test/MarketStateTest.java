package com.jforex.dzjforex.misc.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.stubbing.OngoingStubbing;

import com.dukascopy.api.IDataService;
import com.jforex.dzjforex.brokertime.DummySubmit;
import com.jforex.dzjforex.misc.MarketState;
import com.jforex.dzjforex.testutil.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;

@RunWith(HierarchicalContextRunner.class)
public class MarketStateTest extends CommonUtilForTest {

    private MarketState marketState;

    @Mock
    private IDataService dataServiceMock;
    @Mock
    private DummySubmit dummySubmitMock;
    private final long serverTime = 42L;

    @Before
    public void setUp() {
        marketState = new MarketState(dataServiceMock, dummySubmitMock);
    }

    private OngoingStubbing<Boolean> stubIsOfflineDomain() {
        return when(dataServiceMock.isOfflineTime(serverTime));
    }

    private OngoingStubbing<Boolean> stubWasOffline() {
        return when(dummySubmitMock.wasOffline(serverTime));
    }

    private void assertMarketIsClosed(final boolean isClosed) {
        assertThat(marketState.isClosed(serverTime), equalTo(isClosed));
    }

    @Test
    public void whenServerTimeIsInOfflineDomainMarketIsClosed() {
        stubIsOfflineDomain().thenReturn(true);

        assertMarketIsClosed(true);
    }

    public class WhenServerTimeIsNotInOfflineDomain {

        @Before
        public void setUp() {
            stubIsOfflineDomain().thenReturn(false);
        }

        @Test
        public void whenDummySubmitWasOfflineThenMarketIsClosed() {
            stubWasOffline().thenReturn(true);

            assertMarketIsClosed(true);
        }

        @Test
        public void whenDummySubmitWasOnlineThenMarketIsOpen() {
            stubWasOffline().thenReturn(false);

            assertMarketIsClosed(false);
        }
    }
}
