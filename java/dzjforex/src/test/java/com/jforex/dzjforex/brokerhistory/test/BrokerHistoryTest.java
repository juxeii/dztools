package com.jforex.dzjforex.brokerhistory.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.stubbing.OngoingStubbing;

import com.jforex.dzjforex.brokerhistory.BarFetcher;
import com.jforex.dzjforex.brokerhistory.BrokerHistory;
import com.jforex.dzjforex.brokerhistory.BrokerHistoryData;
import com.jforex.dzjforex.brokerhistory.TickFetcher;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.testutil.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class BrokerHistoryTest extends CommonUtilForTest {

    private BrokerHistory brokerHistory;

    @Mock
    private BarFetcher barFetcherMock;
    @Mock
    private TickFetcher tickFetcherMock;
    @Mock
    private BrokerHistoryData brokerHistoryDataMock;
    private final int historyUnavailable = ZorroReturnValues.HISTORY_UNAVAILABLE.getValue();

    @Before
    public void setUp() {
        brokerHistory = new BrokerHistory(barFetcherMock, tickFetcherMock);
    }

    private OngoingStubbing<String> stubAssetName() {
        return when(brokerHistoryDataMock.assetName());
    }

    private OngoingStubbing<Integer> stubNoOfTickMinutes() {
        return when(brokerHistoryDataMock.periodInMinutes());
    }

    private TestObserver<Integer> subscribe() {
        return brokerHistory
            .get(brokerHistoryDataMock)
            .test();
    }

    @Test
    public void getCallIsDeferred() {
        brokerHistory.get(brokerHistoryDataMock);

        verifyZeroInteractions(barFetcherMock);
        verifyZeroInteractions(tickFetcherMock);
    }

    @Test
    public void historyUnavailableWhenAssetNameIsInvalid() {
        stubAssetName().thenReturn("Invalid");

        subscribe().assertValue(historyUnavailable);
    }

    public class WhenAssetNameIsValid {

        @Before
        public void setUp() {
            stubAssetName().thenReturn(instrumentNameForTest);
        }

        public class BarFetch {

            @Before
            public void setUp() {
                stubNoOfTickMinutes().thenReturn(1);
            }

            private OngoingStubbing<Single<Integer>> stubBarFetchResult() {
                return when(barFetcherMock.run(instrumentForTest, brokerHistoryDataMock));
            }

            @Test
            public void historyUnavailableWhenBarFetcherFails() {
                stubBarFetchResult().thenReturn(Single.error(jfException));

                subscribe().assertValue(historyUnavailable);
            }

            @Test
            public void valueFromBarFetcherIsReturnedWhenBarFetcherSucceeds() {
                final int expectedReturnValue = 42;
                stubBarFetchResult().thenReturn(Single.just(expectedReturnValue));

                subscribe().assertValue(expectedReturnValue);
            }
        }

        public class TickFetch {

            @Before
            public void setUp() {
                stubNoOfTickMinutes().thenReturn(0);
            }

            private OngoingStubbing<Single<Integer>> stubTickFetchResult() {
                return when(tickFetcherMock.run(instrumentForTest, brokerHistoryDataMock));
            }

            @Test
            public void historyUnavailableWhenTickFetcherFails() {
                stubTickFetchResult().thenReturn(Single.error(jfException));

                subscribe().assertValue(historyUnavailable);
            }

            @Test
            public void valueFromBarFetcherIsReturnedWhenTickFetcherSucceeds() {
                final int expectedReturnValue = 27;
                stubTickFetchResult().thenReturn(Single.just(expectedReturnValue));

                subscribe().assertValue(expectedReturnValue);
            }
        }
    }
}
