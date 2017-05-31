package com.jforex.dzjforex.brokersubscribe.test;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.stubbing.OngoingStubbing;

import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.brokeraccount.AccountInfo;
import com.jforex.dzjforex.brokersubscribe.BrokerSubscribe;
import com.jforex.dzjforex.brokersubscribe.Subscription;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.testutil.CommonUtilForTest;
import com.jforex.programming.currency.CurrencyFactory;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Completable;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class BrokerSubscribeTest extends CommonUtilForTest {

    private BrokerSubscribe brokerSubscribe;

    @Mock
    private Subscription subscriptionMock;
    @Mock
    private AccountInfo accountInfoMock;
    @Captor
    private ArgumentCaptor<List<Instrument>> instrumentsCaptor;

    @Before
    public void setUp() {
        brokerSubscribe = new BrokerSubscribe(subscriptionMock, accountInfoMock);
    }

    private TestObserver<Integer> subscribe() {
        return brokerSubscribe
            .forName(instrumentNameForTest)
            .test();
    }

    private OngoingStubbing<Boolean> stubIsSubscribed() {
        return when(subscriptionMock.isSubscribed(instrumentForTest));
    }

    @Test
    public void forNameCallIsDeferred() {
        brokerSubscribe.forName(instrumentNameForTest);

        verifyZeroInteractions(subscriptionMock);
        verifyZeroInteractions(accountInfoMock);
    }

    @Test
    public void anInvalidAssetNameGivesAssetUnavailable() {
        brokerSubscribe
            .forName("Invalid")
            .test()
            .assertValue(ZorroReturnValues.ASSET_UNAVAILABLE.getValue())
            .assertComplete();
    }

    @Test
    public void whenAssetAlreadySubscribedAssetIsAvailable() {
        stubIsSubscribed().thenReturn(true);

        subscribe()
            .assertValue(ZorroReturnValues.ASSET_AVAILABLE.getValue())
            .assertComplete();
    }

    public class WhenAssetNotSubscribed {

        @Before
        public void setUp() {
            stubIsSubscribed().thenReturn(false);

            when(subscriptionMock.set(any())).thenReturn(Completable.complete());
        }

        @Test
        public void whenAssetHasAccountCurrencyOnlyAssetIsSubscribed() {
            when(accountInfoMock.currency()).thenReturn(baseCurrencyForTest);

            subscribe()
                .assertValue(ZorroReturnValues.ASSET_AVAILABLE.getValue())
                .assertComplete();

            verify(subscriptionMock).set(instrumentsCaptor.capture());
            assertThat(instrumentsCaptor.getValue(), containsInAnyOrder(instrumentForTest));
        }

        @Test
        public void whenAssetHasNoAccountCurrencyAssetAndCrossAssetsAreSubscribed() {
            when(accountInfoMock.currency()).thenReturn(CurrencyFactory.BRL);

            subscribe()
                .assertValue(ZorroReturnValues.ASSET_AVAILABLE.getValue())
                .assertComplete();

            verify(subscriptionMock).set(instrumentsCaptor.capture());
            assertThat(instrumentsCaptor.getValue(), containsInAnyOrder(instrumentForTest,
                                                                        Instrument.EURBRL,
                                                                        Instrument.USDBRL));
        }
    }
}
