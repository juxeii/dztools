package com.jforex.dzjforex.brokertime.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.stubbing.OngoingStubbing;

import com.jforex.dzjforex.brokertime.BrokerTime;
import com.jforex.dzjforex.brokertime.BrokerTimeData;
import com.jforex.dzjforex.brokertime.ServerTimeProvider;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.misc.MarketState;
import com.jforex.dzjforex.test.util.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class BrokerTimeTest extends CommonUtilForTest {

    private BrokerTime brokerTime;

    @Mock
    private BrokerTimeData brokerTimeDataMock;
    @Mock
    private ServerTimeProvider serverTimeProviderMock;
    @Mock
    private MarketState marketStateMock;
    private final long serverTime = 42L;

    @Before
    public void setUp() {
        brokerTime = new BrokerTime(clientMock,
                                    serverTimeProviderMock,
                                    marketStateMock);
    }

    private TestObserver<Integer> subscribe() {
        return brokerTime
            .get(brokerTimeDataMock)
            .test();
    }

    private OngoingStubbing<Boolean> stubIsConnected() {
        return when(clientMock.isConnected());
    }

    @Test
    public void getCallIsDeferred() {
        brokerTime.get(brokerTimeDataMock);

        verifyZeroInteractions(clientMock);
        verifyZeroInteractions(serverTimeProviderMock);
        verifyZeroInteractions(marketStateMock);
    }

    @Test
    public void whenClientIsNotConnectedNewLoginIsRequired() {
        stubIsConnected().thenReturn(false);

        subscribe().assertValue(ZorroReturnValues.CONNECTION_LOST_NEW_LOGIN_REQUIRED.getValue());
    }

    public class WhenClientIsConnected {

        @Before
        public void setUp() {
            stubIsConnected().thenReturn(true);
        }

        private OngoingStubbing<Single<Long>> stubGetServerTime() {
            return when(serverTimeProviderMock.get());
        }

        @Test
        public void whenServerTimeFailsInvalidServerTimeIsReturned() {
            stubGetServerTime().thenReturn(Single.error(jfException));

            subscribe().assertValue(ZorroReturnValues.INVALID_SERVER_TIME.getValue());
        }

        public class WhenServerTimeSucceeds {

            @Before
            public void setUp() {
                stubGetServerTime().thenReturn(Single.just(serverTime));
            }

            private OngoingStubbing<Boolean> stubIsMarketClosed() {
                return when(marketStateMock.isClosed(serverTime));
            }

            @Test
            public void serverTimeIsFilled() {
                subscribe();

                verify(brokerTimeDataMock).fill(serverTime);
            }

            @Test
            public void whenMarketIsClosedReturnValueIsOKButMarketClosed() {
                stubIsMarketClosed().thenReturn(true);

                subscribe().assertValue(ZorroReturnValues.CONNECTION_OK_BUT_MARKET_CLOSED.getValue());
            }

            @Test
            public void whenMarketIsOpenReturnValueIsConnectionOK() {
                stubIsMarketClosed().thenReturn(false);

                subscribe().assertValue(ZorroReturnValues.CONNECTION_OK.getValue());
            }
        }
    }
}
