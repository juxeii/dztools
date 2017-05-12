package com.jforex.dzjforex.order;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.jforex.dzjforex.brokeraccount.AccountInfo;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.dzjforex.misc.RxUtility;
import com.jforex.programming.strategy.StrategyUtil;

import io.reactivex.Single;

public class TradeUtility {

    private final OrderLookup orderRepository;
    private final StrategyUtil strategyUtil;
    private final OrderLabelUtil labelUtil;
    private final AccountInfo accountInfo;
    private final PluginConfig pluginConfig;

    private final static Logger logger = LogManager.getLogger(TradeUtility.class);

    public TradeUtility(final OrderLookup orderRepository,
                        final StrategyUtil strategyUtil,
                        final AccountInfo accountInfo,
                        final OrderLabelUtil orderLabel,
                        final PluginConfig pluginConfig) {
        this.orderRepository = orderRepository;
        this.strategyUtil = strategyUtil;
        this.accountInfo = accountInfo;
        this.labelUtil = orderLabel;
        this.pluginConfig = pluginConfig;
    }

    public OrderLabelUtil orderLabelUtil() {
        return labelUtil;
    }

    public Single<Instrument> instrumentForTrading(final String assetName) {
        return isTradingAllowed()
                ? RxUtility.instrumentFromName(assetName)
                : Single.error(new JFException("Trading not allowed for asset " + assetName));
    }

    public Single<IOrder> orderForTrading(final int nTradeID) {
        return isTradingAllowed()
                ? orderByID(nTradeID)
                : Single.error(new JFException("Trading not allowed for nTradeID " + nTradeID));
    }

    private boolean isTradingAllowed() {
        if (!accountInfo.isTradingAllowed()) {
            logger.warn("Trading not allowed since account is in state " + accountInfo.state());
            return false;
        }
        return true;
    }

    public int amountToContracts(final double amount) {
        return (int) (amount * pluginConfig.lotScale());
    }

    public double contractsToAmount(final double contracts) {
        return Math.abs(contracts) / pluginConfig.lotScale();
    }

    public Single<IOrder> orderByID(final int nTradeID) {
        return orderRepository.getByID(nTradeID);
    }

    public double spread(final Instrument instrument) {
        return strategyUtil
            .instrumentUtil(instrument)
            .spread();
    }

    public double currentAsk(final Instrument instrument) {
        return strategyUtil
            .instrumentUtil(instrument)
            .askQuote();
    }

    public double currentBid(final Instrument instrument) {
        return strategyUtil
            .instrumentUtil(instrument)
            .bidQuote();
    }
}
