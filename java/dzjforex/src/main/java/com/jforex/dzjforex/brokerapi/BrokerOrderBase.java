package com.jforex.dzjforex.brokerapi;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.dzjforex.handler.AccountInfo;
import com.jforex.dzjforex.handler.InstrumentHandler;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.strategy.StrategyUtil;

public class BrokerOrderBase {

    protected final StrategyUtil strategyUtil;
    protected final OrderUtil orderUtil;
    protected final AccountInfo accountInfo;
    protected final PluginConfig pluginConfig;

    protected final static Logger logger = LogManager.getLogger(BrokerOrderBase.class);

    public BrokerOrderBase(final StrategyUtil strategyUtil,
                           final AccountInfo accountInfo,
                           final PluginConfig pluginConfig) {
        this.strategyUtil = strategyUtil;
        this.accountInfo = accountInfo;
        this.pluginConfig = pluginConfig;

        orderUtil = strategyUtil.orderUtil();
    }

    protected Optional<Instrument> maybeInstrumentForTrading(final String assetName) {
        return isTradingAllowed()
                ? InstrumentHandler.fromName(assetName)
                : Optional.empty();
    }

    protected boolean isTradingAllowed() {
        if (!accountInfo.isTradingAllowed()) {
            logger.warn("Trading account is not available!");
            return false;
        }
        return true;
    }
}
