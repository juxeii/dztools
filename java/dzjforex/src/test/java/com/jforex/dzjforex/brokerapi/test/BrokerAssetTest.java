//package com.jforex.dzjforex.brokerapi.test;
//
//import static org.hamcrest.Matchers.equalTo;
//import static org.junit.Assert.assertThat;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.Mock;
//
//import com.dukascopy.api.Instrument;
//import com.jforex.dzjforex.brokerapi.BrokerAsset;
//import com.jforex.dzjforex.config.ZorroReturnValues;
//import com.jforex.dzjforex.misc.AccountInfo;
//import com.jforex.dzjforex.test.util.CommonUtilForTest;
//import com.jforex.programming.instrument.InstrumentUtil;
//import com.jforex.programming.strategy.StrategyUtil;
//
//import de.bechte.junit.runners.context.HierarchicalContextRunner;
//
//@RunWith(HierarchicalContextRunner.class)
//public class BrokerAssetTest extends CommonUtilForTest {
//
//    private BrokerAsset brokerAsset;
//
//    @Mock
//    private AccountInfo accountInfoMock;
//    @Mock
//    private StrategyUtil strategyUtilMock;
//    @Mock
//    private InstrumentUtil instrumentUtilMock;
//    private final double assetParams[] = new double[9];
//
//    @Before
//    public void setUp() {
//        when(strategyUtilMock.instrumentUtil(Instrument.EURUSD)).thenReturn(instrumentUtilMock);
//
//        brokerAsset = new BrokerAsset(accountInfoMock, strategyUtilMock);
//    }
//
//    @Test
//    public void anInvalidAssetNameGivesAssetUnavailable() {
//        assertThat(brokerAsset.fillAssetParams("Invalid", assetParams),
//                   equalTo(ZorroReturnValues.ASSET_UNAVAILABLE.getValue()));
//    }
//
//    public class WhenAssetNameIsValid {
//
//        private int returnCode;
//        private final double askQuote = 1.32457;
//        private final double spread = 0.00012;
//        private final double pVolume = 0.0;
//        private final double pPip = Instrument.EURUSD.getPipValue();
//        private final double pPipCost = 0.0923;
//        private final double pLotAmount = 1000;
//        private final double pMarginCost = 9.98;
//        private final double pRollLong = 0.0;
//        private final double pRollShort = 0.0;
//
//        @Before
//        public void setUp() {
//            when(instrumentUtilMock.askQuote()).thenReturn(askQuote);
//            when(instrumentUtilMock.spread()).thenReturn(spread);
//
//            when(accountInfoMock.pipCost(Instrument.EURUSD)).thenReturn(pPipCost);
//            when(accountInfoMock.lotSize()).thenReturn(pLotAmount);
//            when(accountInfoMock.marginPerLot(Instrument.EURUSD)).thenReturn(pMarginCost);
//
//            returnCode = brokerAsset.fillAssetParams("EUR/USD", assetParams);
//        }
//
//        @Test
//        public void returnCodeIsAssetAvailable() {
//            assertThat(returnCode, equalTo(ZorroReturnValues.ASSET_AVAILABLE.getValue()));
//        }
//
//        @Test
//        public void pPriceIsFilled() {
//            assertThat(assetParams[0], equalTo(askQuote));
//        }
//
//        @Test
//        public void pSpreadIsFilled() {
//            assertThat(assetParams[1], equalTo(spread));
//        }
//
//        @Test
//        public void pVolumeIsFilled() {
//            assertThat(assetParams[2], equalTo(pVolume));
//        }
//
//        @Test
//        public void pPipIsFilled() {
//            assertThat(assetParams[3], equalTo(pPip));
//        }
//
//        @Test
//        public void pPipCostIsFilled() {
//            assertThat(assetParams[4], equalTo(pPipCost));
//        }
//
//        @Test
//        public void pLotAmountIsFilled() {
//            assertThat(assetParams[5], equalTo(pLotAmount));
//        }
//
//        @Test
//        public void pMarginCostIsFilled() {
//            assertThat(assetParams[6], equalTo(pMarginCost));
//        }
//
//        @Test
//        public void pRollLongIsFilled() {
//            assertThat(assetParams[7], equalTo(pRollLong));
//        }
//
//        @Test
//        public void pRollShortIsFilled() {
//            assertThat(assetParams[8], equalTo(pRollShort));
//        }
//    }
//}
