package com.jforex.dzjforex.brokerasset;

public class BrokerAssetData {

    private final String assetName;
    private final double assetParams[];

    public BrokerAssetData(final String assetName,
                           final double assetParams[]) {
        this.assetName = assetName;
        this.assetParams = assetParams;
    }

    public String assetName() {
        return assetName;
    }

    public void fill(final double pPrice,
                     final double pSpread,
                     final double pVolume,
                     final double pPip,
                     final double pPipCost,
                     final double pLotAmount,
                     final double pMarginCost,
                     final double pRollLong,
                     final double pRollShort) {
        assetParams[0] = pPrice;
        assetParams[1] = pSpread;
        assetParams[2] = pVolume;
        assetParams[3] = pPip;
        assetParams[4] = pPipCost;
        assetParams[5] = pLotAmount;
        assetParams[6] = pMarginCost;
        assetParams[7] = pRollLong;
        assetParams[8] = pRollShort;
    }
}
