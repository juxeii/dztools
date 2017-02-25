package com.jforex.dzjforex.brokerapi;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.handler.InstrumentRepository;
import com.jforex.dzjforex.misc.AccountInfo;
import com.jforex.programming.instrument.InstrumentUtil;
import com.jforex.programming.strategy.StrategyUtil;

public class BrokerAsset {

    private final AccountInfo accountInfo;
    private final StrategyUtil strategyUtil;

    private static final double valueNotSupported = 0.0;
    private final static Logger logger = LogManager.getLogger(BrokerAsset.class);

    public BrokerAsset(final AccountInfo accountInfo,
                       final StrategyUtil strategyUtil) {
        this.accountInfo = accountInfo;
        this.strategyUtil = strategyUtil;
    }

    public int fillAssetParams(final String assetName,
                               final double assetParams[]) {
        final Optional<Instrument> maybeInstrument = InstrumentRepository.maybeFromName(assetName);
        return maybeInstrument.isPresent()
                ? fillAssetParamsForValidInstrument(maybeInstrument.get(), assetParams)
                : ZorroReturnValues.ASSET_UNAVAILABLE.getValue();
    }

    private int fillAssetParamsForValidInstrument(final Instrument instrument,
                                                  final double assetParams[]) {
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

        assetParams[0] = pPrice;
        assetParams[1] = pSpread;
        assetParams[2] = pVolume;
        assetParams[3] = pPip;
        assetParams[4] = pPipCost;
        assetParams[5] = pLotAmount;
        assetParams[6] = pMarginCost;
        assetParams[7] = pRollLong;
        assetParams[8] = pRollShort;

        logger.trace("BrokerAsset instrument params for " + instrument + ": \n"
                + " pPrice: " + pPrice + "\n"
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
