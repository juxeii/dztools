package com.jforex.dzjforex.brokerapi.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.ICurrency;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.system.IClient;
import com.jforex.dzjforex.brokeraccount.AccountInfo;
import com.jforex.dzjforex.brokersubscribe.BrokerSubscribe;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.test.util.CommonUtilForTest;
import com.jforex.programming.currency.CurrencyFactory;

import de.bechte.junit.runners.context.HierarchicalContextRunner;

@RunWith(HierarchicalContextRunner.class)
public class BrokerSubscribeTest extends CommonUtilForTest {

    private BrokerSubscribe brokerSubscribe;

    @Mock
    private IClient clientMock;
    @Mock
    private AccountInfo accountInfoMock;
    private final ICurrency accountCurrency = CurrencyFactory.EUR;
    private String assetName = "EUR/USD";

    @Before
    public void setUp() {
        when(accountInfoMock.currency()).thenReturn(accountCurrency);

        brokerSubscribe = new BrokerSubscribe(clientMock, accountInfoMock);
    }

    @Test
    public void returnCodeIsAssetUnAvailableWhenAssetNameIsInvalid() {
        assertThat(brokerSubscribe.subscribe("Invalid"), equalTo(ZorroReturnValues.ASSET_UNAVAILABLE.getValue()));
    }

    @Test
    public void returnCodeIsAssetAvailableWhenAssetNameIsValid() {
        assertThat(brokerSubscribe.subscribe(assetName), equalTo(ZorroReturnValues.ASSET_AVAILABLE.getValue()));
    }

    @Test
    public void subscribedInstrumentsReturnsSetFromIClient() {
        final Set<Instrument> subscribedInstruments = new HashSet<>();

        when(clientMock.getSubscribedInstruments()).thenReturn(subscribedInstruments);

        assertThat(brokerSubscribe.subscribedInstruments(), equalTo(subscribedInstruments));
    }

    public class WhenAccountCurrencyIsBaseCurrency {

        @Before
        public void setUp() {
            when(accountInfoMock.currency()).thenReturn(CurrencyFactory.EUR);
        }

        @Test
        public void instrumentSubscriberCallContainsOnlyEURUSD() {
            assetName = "EUR/USD";

            brokerSubscribe.subscribe(assetName);

            assertInstrumentSubscriberCall(Instrument.EURUSD);
        }

        @Test
        public void instrumentSubscriberCallContainsGBPAUDAndEURGBPAndEURAUD() {
            assetName = "GBP/AUD";

            brokerSubscribe.subscribe(assetName);

            assertInstrumentSubscriberCall(Instrument.GBPAUD,
                                           Instrument.EURGBP,
                                           Instrument.EURAUD);
        }
    }

    public class WhenAccountCurrencyIsQuoteCurrency {

        @Before
        public void setUp() {
            when(accountInfoMock.currency()).thenReturn(CurrencyFactory.JPY);
        }

        @Test
        public void instrumentSubscriberCallContainsOnlyUSDJPY() {
            assetName = "USD/JPY";

            brokerSubscribe.subscribe(assetName);

            assertInstrumentSubscriberCall(Instrument.USDJPY);
        }

        @Test
        public void instrumentSubscriberCallContainsEURUSDAndEURJPYAndUSDJPY() {
            assetName = "EUR/USD";

            brokerSubscribe.subscribe(assetName);

            assertInstrumentSubscriberCall(Instrument.EURUSD,
                                           Instrument.EURJPY,
                                           Instrument.USDJPY);
        }
    }

    private void assertInstrumentSubscriberCall(final Instrument... instruments) {
        final List<Instrument> ints = Arrays.asList(instruments);
        verify(clientMock).setSubscribedInstruments(argThat(is -> is.size() == instruments.length
                && is.containsAll(ints)));
    }
}
