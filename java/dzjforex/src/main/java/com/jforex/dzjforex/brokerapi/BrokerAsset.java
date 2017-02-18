package com.jforex.dzjforex.brokerapi;

import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.config.ReturnCodes;
import com.jforex.dzjforex.handler.AccountInfo;
import com.jforex.dzjforex.handler.InstrumentHandler;
import com.jforex.dzjforex.misc.TradeCalculation;
import com.jforex.programming.instrument.InstrumentUtil;
import com.jforex.programming.strategy.StrategyUtil;

public class BrokerAsset {

    private final AccountInfo accountInfo;
    private final TradeCalculation tradeCalculation;
    private final StrategyUtil strategyUtil;

    public BrokerAsset(final AccountInfo accountInfo,
                       final TradeCalculation tradeCalculation,
                       final StrategyUtil strategyUtil) {
        this.accountInfo = accountInfo;
        this.tradeCalculation = tradeCalculation;
        this.strategyUtil = strategyUtil;
    }

    public int handle(final String instrumentName,
                      final double assetParams[]) {
        return InstrumentHandler.executeForInstrument(instrumentName,
                                                      instrument -> fillAssetParams(instrument, assetParams),
                                                      ReturnCodes.ASSET_UNAVAILABLE);
    }

    private int fillAssetParams(final Instrument instrument,
                                final double assetParams[]) {
        final InstrumentUtil instrumentUtil = strategyUtil.instrumentUtil(instrument);

        final double pPrice = instrumentUtil.askQuote();
        final double pSpread = instrumentUtil.spread();
        final double pVolume = 0.0; // currently not supported
        final double pPip = instrument.getPipValue();
        final double pPipCost = tradeCalculation.pipCost(instrument);
        final double pLotAmount = accountInfo.lotSize();
        final double pMarginCost = tradeCalculation.marginPerLot(instrument);
        final double pRollLong = 0.0; // currently not supported
        final double pRollShort = 0.0; // currently not supported

        assetParams[0] = pPrice;
        assetParams[1] = pSpread;
        assetParams[2] = pVolume;
        assetParams[3] = pPip;
        assetParams[4] = pPipCost;
        assetParams[5] = pLotAmount;
        assetParams[6] = pMarginCost;
        assetParams[7] = pRollLong;
        assetParams[8] = pRollShort;

        return ReturnCodes.ASSET_AVAILABLE;
    }
}
