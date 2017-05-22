package com.jforex.dzjforex.brokerasset.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.jforex.dzjforex.brokerasset.BrokerAssetData;
import com.jforex.dzjforex.test.util.CommonUtilForTest;

public class BrokerAssetDataTest extends CommonUtilForTest {

    private BrokerAssetData brokerAssetData;

    private final double assetParams[] = new double[9];
    private final double pPrice = 1.1203;
    private final double pSpread = 1.7;
    private final double pVolume = 3205000;
    private final double pPip = 1.0;
    private final double pPipCost = 1.1306;
    private final double pLotAmount = 20050;
    private final double pMarginCost = 43.56;
    private final double pRollLong = 0.2;
    private final double pRollShort = 0.3;

    @Before
    public void setUp() {
        brokerAssetData = new BrokerAssetData(instrumentNameForTest, assetParams);

        brokerAssetData.fill(pPrice,
                             pSpread,
                             pVolume,
                             pPip,
                             pPipCost,
                             pLotAmount,
                             pMarginCost,
                             pRollLong,
                             pRollShort);
    }

    private void assertFillValueAtIndex(final double value,
                                        final int index) {
        assertThat(assetParams[index], equalTo(value));
    }

    @Test
    public void assertInstrumentName() {
        assertThat(brokerAssetData.instrumentName(), equalTo(instrumentNameForTest));
    }

    @Test
    public void priceIsCorrectFilled() {
        assertFillValueAtIndex(pPrice, 0);
    }

    @Test
    public void spreadIsCorrectFilled() {
        assertFillValueAtIndex(pSpread, 1);
    }

    @Test
    public void volumeIsCorrectFilled() {
        assertFillValueAtIndex(pVolume, 2);
    }

    @Test
    public void pipIsCorrectFilled() {
        assertFillValueAtIndex(pPip, 3);
    }

    @Test
    public void pipCostIsCorrectFilled() {
        assertFillValueAtIndex(pPipCost, 4);
    }

    @Test
    public void lotAmountIsCorrectFilled() {
        assertFillValueAtIndex(pLotAmount, 5);
    }

    @Test
    public void marginCostIsCorrectFilled() {
        assertFillValueAtIndex(pMarginCost, 6);
    }

    @Test
    public void rollLongIsCorrectFilled() {
        assertFillValueAtIndex(pRollLong, 7);
    }

    @Test
    public void rollShortIsCorrectFilled() {
        assertFillValueAtIndex(pRollShort, 8);
    }
}
