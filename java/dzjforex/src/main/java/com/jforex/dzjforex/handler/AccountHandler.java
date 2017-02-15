package com.jforex.dzjforex.handler;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IAccount;
import com.dukascopy.api.ICurrency;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.dzjforex.config.ReturnCodes;
import com.jforex.dzjforex.misc.InstrumentProvider;
import com.jforex.programming.instrument.InstrumentUtil;
import com.jforex.programming.math.CalculationUtil;
import com.jforex.programming.strategy.StrategyUtil;

public class AccountHandler {

    private final IAccount account;
    private final StrategyUtil strategyUtil;
    private final CalculationUtil calculationUtil;
    private final ICurrency accountCurrency;
    private final String accountID;
    private final double leverage;
    private final double lotSize;
    private final double lotMargin;

    private final static Logger logger = LogManager.getLogger(AccountHandler.class);

    public AccountHandler(final IAccount account,
                          final StrategyUtil strategyUtil,
                          final CalculationUtil calculationUtil,
                          final PluginConfig pluginConfig) {
        this.account = account;
        this.strategyUtil = strategyUtil;
        this.calculationUtil = calculationUtil;

        accountCurrency = account.getAccountCurrency();
        accountID = account.getAccountId();
        leverage = account.getLeverage();
        lotSize = pluginConfig.LOT_SIZE();
        lotMargin = lotSize / leverage;
    }

    public double getBalance() {
        return account.getBalance();
    }

    public double getEquity() {
        return account.getEquity();
    }

    public String getID() {
        return accountID;
    }

    public ICurrency getCurrency() {
        return accountCurrency;
    }

    public double getTradeValue() {
        return account.getEquity() - account.getBalance();
    }

    public double getFreeMargin() {
        return account.getCreditLine() / leverage;
    }

    public double getUsedMargin() {
        return account.getEquity() - getFreeMargin();
    }

    public double getLeverage() {
        return leverage;
    }

    public boolean isConnected() {
        return account.isConnected();
    }

    public double getPipCost(final Instrument instrument) {
        final double pipCost = calculationUtil.pipValueInCurrency(lotSize,
                                                                  instrument,
                                                                  accountCurrency,
                                                                  OfferSide.ASK);
        logger.debug("Pipcost for lotSize " + lotSize
                + " and instrument " + instrument
                + " is " + pipCost);
        return pipCost;
    }

    public double getMarginForLot(final Instrument instrument) {
        if (accountCurrency == instrument.getPrimaryJFCurrency())
            return lotMargin;

        final double conversionLot = calculationUtil.convertAmount(lotSize,
                                                                   instrument.getPrimaryJFCurrency(),
                                                                   accountCurrency,
                                                                   OfferSide.ASK);
        final double marginCost = conversionLot / leverage;
        logger.debug("marginCost for conversion instrument " + instrument.getPrimaryJFCurrency()
                + " and  conversionLot " + conversionLot
                + " and leverage " + leverage);
        return marginCost;
    }

    public boolean isTradingPossible() {
        if (account.getAccountState() != IAccount.AccountState.OK) {
            logger.debug("Account state " + account.getAccountState() + " is invalid for trading!");
            return false;
        }
        return account.isConnected();
    }

    public int doBrokerAsset(final String instrumentName,
                             final double assetParams[]) {
        final Optional<Instrument> instrumentOpt = InstrumentProvider.fromName(instrumentName);
        if (!instrumentOpt.isPresent())
            return ReturnCodes.ASSET_UNAVAILABLE;

        return fillAssetParams(instrumentOpt.get(), assetParams);
    }

    private int fillAssetParams(final Instrument instrument,
                                final double assetParams[]) {
        final InstrumentUtil instrumentUtil = strategyUtil.instrumentUtil(instrument);

        final double pPrice = instrumentUtil.askQuote();
        final double pSpread = instrumentUtil.spread();
        final double pVolume = 0.0; // currently not supported
        final double pPip = instrument.getPipValue();
        final double pPipCost = getPipCost(instrument);
        final double pLotAmount = lotSize;
        final double pMarginCost = getMarginForLot(instrument);
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

    public int doBrokerAccount(final double accountInfoParams[]) {
        accountInfoParams[0] = getBalance();
        accountInfoParams[1] = getTradeValue();
        accountInfoParams[2] = getUsedMargin();

        return ReturnCodes.ACCOUNT_AVAILABLE;
    }
}
