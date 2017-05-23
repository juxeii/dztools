package com.jforex.dzjforex.brokerhistory.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.jforex.dzjforex.brokerhistory.BarFetcher;
import com.jforex.dzjforex.brokerhistory.BrokerHistory;
import com.jforex.dzjforex.brokerhistory.BrokerHistoryData;
import com.jforex.dzjforex.brokerhistory.TickFetcher;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.test.util.CommonUtilForTest;

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

    private void setAssetName(final String assetName) {
        when(brokerHistoryDataMock.instrumentName()).thenReturn(assetName);
    }

    private void setNoOfTickMinutes(final int noOfTickMinutes) {
        when(brokerHistoryDataMock.noOfTickMinutes()).thenReturn(noOfTickMinutes);
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
        setAssetName("Invalid");

        subscribe().assertValue(historyUnavailable);
    }

    public class WhenAssetNameIsValid {

        @Before
        public void setUp() {
            setAssetName(instrumentNameForTest);
        }

        public class BarFetch {

            @Before
            public void setUp() {
                setNoOfTickMinutes(1);
            }

            private void setBarFetchResult(final Single<Integer> result) {
                when(barFetcherMock.run(instrumentForTest, brokerHistoryDataMock))
                    .thenReturn(result);
            }

            @Test
            public void historyUnavailableWhenBarFetcherFails() {
                setBarFetchResult(Single.error(jfException));

                subscribe().assertValue(historyUnavailable);
            }

            @Test
            public void valueFromBarFetcherIsReturnedWhenBarFetcherSucceeds() {
                final int expectedReturnValue = 42;
                setBarFetchResult(Single.just(expectedReturnValue));

                subscribe().assertValue(expectedReturnValue);
            }
        }

        public class TickFetch {

            @Before
            public void setUp() {
                setNoOfTickMinutes(0);
            }

            private void setTickFetchResult(final Single<Integer> result) {
                when(tickFetcherMock.run(instrumentForTest, brokerHistoryDataMock))
                    .thenReturn(result);
            }

            @Test
            public void historyUnavailableWhenTickFetcherFails() {
                setTickFetchResult(Single.error(jfException));

                subscribe().assertValue(historyUnavailable);
            }

            @Test
            public void valueFromBarFetcherIsReturnedWhenTickFetcherSucceeds() {
                final int expectedReturnValue = 27;
                setTickFetchResult(Single.just(expectedReturnValue));

                subscribe().assertValue(expectedReturnValue);
            }
        }
    }
}
