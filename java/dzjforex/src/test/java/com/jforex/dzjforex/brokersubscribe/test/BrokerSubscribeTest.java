package com.jforex.dzjforex.brokersubscribe.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.Instrument;
import com.google.common.collect.Sets;
import com.jforex.dzjforex.brokeraccount.AccountInfo;
import com.jforex.dzjforex.brokersubscribe.BrokerSubscribe;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.testutil.CommonUtilForTest;
import com.jforex.programming.currency.CurrencyFactory;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class BrokerSubscribeTest extends CommonUtilForTest {

    private BrokerSubscribe brokerSubscribe;

    @Mock
    private AccountInfo accountInfoMock;
    private final Set<Instrument> subscribedInstruments = Sets.newHashSet();

    @Before
    public void setUp() {
        when(clientMock.getSubscribedInstruments()).thenReturn(subscribedInstruments);

        brokerSubscribe = new BrokerSubscribe(clientMock, accountInfoMock);
    }

    private TestObserver<Integer> subscribe() {
        return brokerSubscribe
            .forName(instrumentNameForTest)
            .test();
    }

    @Test
    public void subscribedInstrumentsReturnsCorrectSet() {
        assertThat(brokerSubscribe.subscribedInstruments(), equalTo(subscribedInstruments));
    }

    @Test
    public void anInvalidAssetNameGivesAssetUnavailable() {
        brokerSubscribe
            .forName("Invalid")
            .test()
            .assertValue(ZorroReturnValues.ASSET_UNAVAILABLE.getValue());
    }

    public class WhenAssetAlreadySubscribed {

        @Before
        public void setUp() {
            subscribedInstruments.add(instrumentForTest);
        }

        @Test
        public void returnValueIsAssetAvailable() {
            subscribe().assertValue(ZorroReturnValues.ASSET_AVAILABLE.getValue());
        }

        @Test
        public void noIClientCallForSubscribe() {
            subscribe();

            verify(clientMock, never()).setSubscribedInstruments(any());
        }
    }

    public class WhenAssetNotSubscribed {

        @Test
        public void whenAssetHasAccountCurrencyOnlyAssetIsSubscribed() {
            when(accountInfoMock.currency()).thenReturn(baseCurrencyForTest);

            subscribe().assertValue(ZorroReturnValues.ASSET_AVAILABLE.getValue());

            verify(clientMock).setSubscribedInstruments(argThat(set -> set.contains(instrumentForTest)
                    && set.size() == 1));
        }

        @Test
        public void whenAssetHasNoAccountCurrencyAssetAndCrossAssetsAreSubscribed() {
            when(accountInfoMock.currency()).thenReturn(CurrencyFactory.BRL);

            subscribe().assertValue(ZorroReturnValues.ASSET_AVAILABLE.getValue());

            verify(clientMock).setSubscribedInstruments(argThat(set -> set.contains(instrumentForTest)
                    && set.contains(Instrument.EURBRL)
                    && set.contains(Instrument.USDBRL)
                    && set.size() == 3));
        }
    }
}
