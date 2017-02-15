package com.jforex.dzjforex.handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.ZorroLogger;
import com.jforex.dzjforex.dataprovider.AccountInfo;
import com.jforex.dzjforex.dataprovider.ServerTime;
import com.jforex.dzjforex.misc.DateTimeUtils;
import com.jforex.dzjforex.misc.InstrumentUtils;
import com.jforex.dzjforex.settings.ReturnCodes;
import com.jforex.programming.instrument.InstrumentUtil;
import com.jforex.programming.strategy.StrategyUtil;

public class AccountHandler {

    private final StrategyUtil strategyUtil;
    private final AccountInfo accountInfo;
    private final ServerTime serverTime;
    private final DateTimeUtils dateTimeUtils;

    private final static Logger logger = LogManager.getLogger(AccountHandler.class);

    public AccountHandler(final StrategyUtil strategyUtil,
                          final AccountInfo accountInfo,
                          final ServerTime serverTime,
                          final DateTimeUtils dateTimeUtils) {
        this.strategyUtil = strategyUtil;
        this.accountInfo = accountInfo;
        this.serverTime = serverTime;
        this.dateTimeUtils = dateTimeUtils;
    }

    public int doBrokerTime(final double serverTimeData[]) {
        serverTimeData[0] = DateTimeUtils.getOLEDateFromMillis(serverTime.get());

        final boolean isMarketOffline = dateTimeUtils.isMarketOffline();
        if (isMarketOffline)
            logger.debug("Market is offline");

        return isMarketOffline
                ? ReturnCodes.CONNECTION_OK_BUT_MARKET_CLOSED
                : ReturnCodes.CONNECTION_OK;
    }

    public int doBrokerAsset(final String instrumentName,
                             final double assetParams[]) {
        final Instrument instrument = InstrumentUtils.getByName(instrumentName);
        if (instrument == null)
            return ReturnCodes.ASSET_UNAVAILABLE;

        return fillAssetParams(instrument, assetParams);
    }

    private int fillAssetParams(final Instrument instrument,
                                final double assetParams[]) {
        final InstrumentUtil instrumentUtil = strategyUtil.instrumentUtil(instrument);
        final ITick tick = instrumentUtil.tickQuote();
        if (tick == null) {
            logger.warn("No data for " + instrument + " available!");
            ZorroLogger.log("No data for " + instrument + " available!");
            return ReturnCodes.ASSET_UNAVAILABLE;
        }

//        final double pipCost = accountInfo.getPipCost(instrument, OfferSide.ASK);
//        if (pipCost == 0f)
//            return ReturnCodes.ASSET_UNAVAILABLE;
//        assetParams[4] = pipCost;

//        final double marginForLot = accountInfo.getMarginForLot(instrument);
//        if (marginForLot == 0f)
//            return ReturnCodes.ASSET_UNAVAILABLE;
//        assetParams[6] = marginForLot;

        assetParams[0] = tick.getAsk();
        assetParams[1] = instrumentUtil.spread();
        // Volume: not supported for Forex
        assetParams[2] = 0f;
        assetParams[3] = instrument.getPipValue();
        // assetParams[5] = DukascopyParams.LOT_SIZE;
        // RollLong : currently not available by Dukascopy
        assetParams[7] = 0f;
        // RollShort: currently not available by Dukascopy
        assetParams[8] = 0f;

        return ReturnCodes.ASSET_AVAILABLE;
    }

    public int doBrokerAccount(final double accountInfoParams[]) {
        accountInfoParams[0] = accountInfo.getBalance();
        accountInfoParams[1] = accountInfo.getTradeValue();
        accountInfoParams[2] = accountInfo.getUsedMargin();

        return ReturnCodes.ACCOUNT_AVAILABLE;
    }
}
