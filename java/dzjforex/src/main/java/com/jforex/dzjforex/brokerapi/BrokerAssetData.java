package com.jforex.dzjforex.brokerapi;

public class BrokerAssetData {

    private final String instrumentName;
    private final double assetParams[];

    public BrokerAssetData(final String instrumentName,
                           final double assetParams[]) {
        this.instrumentName = instrumentName;
        this.assetParams = assetParams;
    }

    public String instrumentName() {
        return instrumentName;
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
