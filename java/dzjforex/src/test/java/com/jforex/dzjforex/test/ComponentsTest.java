package com.jforex.dzjforex.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.Instrument;
import com.google.common.collect.Sets;
import com.jforex.dzjforex.Components;
import com.jforex.dzjforex.SystemComponents;
import com.jforex.dzjforex.brokerlogin.BrokerLoginData;
import com.jforex.dzjforex.testutil.CommonUtilForTest;
import com.jforex.programming.client.ClientUtil;

import de.bechte.junit.runners.context.HierarchicalContextRunner;

@RunWith(HierarchicalContextRunner.class)
public class ComponentsTest extends CommonUtilForTest {

    private Components components;

    @Mock
    private SystemComponents systemComponentsMock;
    @Mock
    private ClientUtil clientUtilMock;
    @Mock
    private BrokerLoginData brokerLoginDataMock;

    @Before
    public void setUp() {
        setUpMocks();

        components = new Components(systemComponentsMock);
    }

    private void setUpMocks() {
        when(systemComponentsMock.client()).thenReturn(clientMock);
        when(systemComponentsMock.clientUtil()).thenReturn(clientUtilMock);
        when(systemComponentsMock.infoStrategy()).thenReturn(infoStrategyMock);
        when(systemComponentsMock.pluginConfig()).thenReturn(pluginConfigMock);

        when(pluginConfigMock.zorroProgressInterval()).thenReturn(1500L);
        when(pluginConfigMock.cacheDirectory()).thenReturn("cacheDirectory");
    }

    @Test
    public void assertValidPluginConfig() {
        assertNotNull(components.pluginConfig());
    }

    @Test
    public void assertValidIClient() {
        assertNotNull(components.client());
    }

    @Test
    public void assertValidZorro() {
        assertNotNull(components.zorro());
    }

    @Test
    public void assertBrokerLogin() {
        assertNotNull(components.brokerLogin());
    }

    public class AfterStartStrategy {

        private final long strategyID = 42L;
        private long startedStrategyID;

        @Before
        public void setUp() {
            when(clientMock.startStrategy(infoStrategyMock)).thenReturn(strategyID);

            startedStrategyID = components.startAndInitStrategyComponents(brokerLoginDataMock);
        }

        @Test
        public void instrumentEURUSDIsSubscribed() {
            verify(clientMock).setSubscribedInstruments(Sets.newHashSet(Instrument.EURUSD));
        }

        @Test
        public void infoStrategyIsStarted() {
            verify(clientMock).startStrategy(infoStrategyMock);
        }

        @Test
        public void assertBrokerLoginDataIsFilled() {
            verify(brokerLoginDataMock).fillAccounts(any());
        }

        @Test
        public void strategyIDIsReturned() {
            assertThat(startedStrategyID, equalTo(strategyID));
        }

        @Test
        public void assertBrokerTime() {
            assertNotNull(components.brokerTime());
        }

        @Test
        public void assertBrokerSubscribe() {
            assertNotNull(components.brokerSubscribe());
        }

        @Test
        public void assertBrokerAsset() {
            assertNotNull(components.brokerAsset());
        }

        @Test
        public void assertBrokerAccount() {
            assertNotNull(components.brokerAccount());
        }

        @Test
        public void assertBrokerHistory() {
            assertNotNull(components.brokerHistory());
        }

        @Test
        public void assertBrokerTrade() {
            assertNotNull(components.brokerTrade());
        }

        @Test
        public void assertBrokerBuy() {
            assertNotNull(components.brokerBuy());
        }

        @Test
        public void assertBbrokerSell() {
            assertNotNull(components.brokerSell());
        }

        @Test
        public void assertbrokerStop() {
            assertNotNull(components.brokerStop());
        }

        @Test
        public void assertTradeUtility() {
            assertNotNull(components.tradeUtility());
        }
    }
}
