package com.jforex.dzjforex.brokerasset;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.brokeraccount.AccountInfo;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.misc.RxUtility;
import com.jforex.programming.instrument.InstrumentFactory;
import com.jforex.programming.instrument.InstrumentUtil;
import com.jforex.programming.strategy.StrategyUtil;

public class BrokerAsset {

    private final AccountInfo accountInfo;
    private final StrategyUtil strategyUtil;

    private final static double valueNotSupported = 0.0;
    private final static Logger logger = LogManager.getLogger(BrokerAsset.class);

    public BrokerAsset(final AccountInfo accountInfo,
                       final StrategyUtil strategyUtil) {
        this.accountInfo = accountInfo;
        this.strategyUtil = strategyUtil;
    }

    public int fillAssetParams(final BrokerAssetData brokerAssetData) {
        return RxUtility
            .optionalToMaybe(InstrumentFactory.maybeFromName(brokerAssetData.instrumentName()))
            .map(instrument -> fillAssetParamsForValidInstrument(instrument, brokerAssetData))
            .defaultIfEmpty(ZorroReturnValues.ASSET_UNAVAILABLE.getValue())
            .blockingGet();
    }

    private int fillAssetParamsForValidInstrument(final Instrument instrument,
                                                  final BrokerAssetData brokerAssetData) {
        final InstrumentUtil instrumentUtil = strategyUtil.instrumentUtil(instrument);

        final double pPrice = instrumentUtil.askQuote();
        final double pSpread = instrumentUtil.spread();
        final double pVolume = valueNotSupported;
        final double pPip = instrument.getPipValue();
        final double pPipCost = accountInfo.pipCost(instrument);
        final double pLotAmount = accountInfo.lotSize();
        final double pMarginCost = accountInfo.marginPerLot(instrument);
        final double pRollLong = valueNotSupported;
        final double pRollShort = valueNotSupported;
        brokerAssetData.fill(pPrice,
                             pSpread,
                             pVolume,
                             pPip,
                             pPipCost,
                             pLotAmount,
                             pMarginCost,
                             pRollLong,
                             pRollShort);
        logger.trace("BrokerAsset values for " + instrument + ": \n"
                + " pAskPrice: " + pPrice + "\n"
                + " pBidPrice: " + instrumentUtil.bidQuote() + "\n"
                + " pSpread: " + pSpread + "\n"
                + " pVolume: " + pVolume + "\n"
                + " pPip: " + pPip + "\n"
                + " pPipCost: " + pPipCost + "\n"
                + " pLotAmount: " + pLotAmount + "\n"
                + " pMarginCost: " + pMarginCost + "\n"
                + " pRollLong: " + pRollLong + "\n"
                + " pRollShort: " + pRollShort);

        return ZorroReturnValues.ASSET_AVAILABLE.getValue();
    }
}
