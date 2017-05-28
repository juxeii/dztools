package com.jforex.dzjforex.brokerasset.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.jforex.dzjforex.brokeraccount.AccountInfo;
import com.jforex.dzjforex.brokerasset.BrokerAsset;
import com.jforex.dzjforex.brokerasset.BrokerAssetData;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.misc.PriceProvider;
import com.jforex.dzjforex.testutil.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class BrokerAssetTest extends CommonUtilForTest {

    private BrokerAsset brokerAsset;

    @Mock
    private AccountInfo accountInfoMock;
    @Mock
    private PriceProvider priceProviderMock;
    @Mock
    private BrokerAssetData brokerAssetDataMock;
    private final double valueNotSupported = 0.0;
    private final double ask = 1.32457;
    private final double spread = 0.00012;
    private final double pVolume = valueNotSupported;
    private final double pPip = instrumentForTest.getPipValue();
    private final double pPipCost = 0.0923;
    private final double pLotAmount = 1000;
    private final double pMarginCost = 9.98;
    private final double pRollLong = valueNotSupported;
    private final double pRollShort = valueNotSupported;

    @Before
    public void setUp() {
        setUpMocks();

        brokerAsset = new BrokerAsset(accountInfoMock, priceProviderMock);
    }

    private void setUpMocks() {
        when(accountInfoMock.pipCost(instrumentForTest)).thenReturn(pPipCost);
        when(accountInfoMock.lotSize()).thenReturn(pLotAmount);
        when(accountInfoMock.marginPerLot(instrumentForTest)).thenReturn(pMarginCost);

        when(priceProviderMock.ask(instrumentForTest)).thenReturn(ask);
        when(priceProviderMock.spread(instrumentForTest)).thenReturn(spread);
    }

    private TestObserver<Integer> subscribe() {
        return brokerAsset
            .fillParams(brokerAssetDataMock)
            .test();
    }

    @Test
    public void anInvalidAssetNameGivesAssetUnavailable() {
        when(brokerAssetDataMock.instrumentName()).thenReturn("Invalid");

        subscribe().assertValue(ZorroReturnValues.ASSET_UNAVAILABLE.getValue());
    }

    public class WhenAssetNameIsValid {

        @Before
        public void setUp() {
            when(brokerAssetDataMock.instrumentName()).thenReturn(instrumentNameForTest);
        }

        @Test
        public void returnCodeIsAssetAvailable() {
            subscribe().assertValue(ZorroReturnValues.ASSET_AVAILABLE.getValue());
        }

        @Test
        public void fillIsCalledOnBrokerAssetData() {
            subscribe();

            verify(brokerAssetDataMock).fill(ask,
                                             spread,
                                             pVolume,
                                             pPip,
                                             pPipCost,
                                             pLotAmount,
                                             pMarginCost,
                                             pRollLong,
                                             pRollShort);
        }
    }
}
