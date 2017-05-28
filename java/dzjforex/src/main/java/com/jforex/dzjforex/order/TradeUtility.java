package com.jforex.dzjforex.order;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.jforex.dzjforex.brokeraccount.AccountInfo;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.dzjforex.misc.RxUtility;
import com.jforex.programming.order.task.params.RetryParams;

import io.reactivex.Maybe;
import io.reactivex.Single;

public class TradeUtility {

    private final OrderLookup orderLookup;
    private final OrderLabelUtil orderLabelUtil;
    private final AccountInfo accountInfo;
    private final PluginConfig pluginConfig;

    private final static Logger logger = LogManager.getLogger(TradeUtility.class);

    public TradeUtility(final OrderLookup orderLookup,
                        final AccountInfo accountInfo,
                        final OrderLabelUtil orderLabelUtil,
                        final RetryParams retryParams,
                        final PluginConfig pluginConfig) {
        this.orderLookup = orderLookup;
        this.accountInfo = accountInfo;
        this.orderLabelUtil = orderLabelUtil;
        this.pluginConfig = pluginConfig;
    }

    public OrderLabelUtil orderLabelUtil() {
        return orderLabelUtil;
    }

    public int amountToContracts(final double amount) {
        return (int) (amount * pluginConfig.lotScale());
    }

    public double contractsToAmount(final double contracts) {
        return Math.abs(contracts) / pluginConfig.lotScale();
    }

    public OrderCommand orderCommandForContracts(final int contracts) {
        return contracts > 0
                ? OrderCommand.BUY
                : OrderCommand.SELL;
    }

    public Maybe<IOrder> orderByID(final int orderID) {
        return orderLookup.getByID(orderID);
    }

    public Single<Instrument> instrumentForTrading(final String assetName) {
        return isTradingAllowed()
                ? RxUtility.instrumentFromName(assetName)
                : Single.error(new JFException("Trading not allowed for asset " + assetName));
    }

    public Single<IOrder> orderForTrading(final int orderID) {
        return isTradingAllowed()
                ? orderByID(orderID).toSingle()
                : Single.error(new JFException("Trading not allowed for nTradeID " + orderID));
    }

    private boolean isTradingAllowed() {
        if (!accountInfo.isTradingAllowed()) {
            logger.warn("Trading not allowed since account is in state " + accountInfo.state());
            return false;
        }
        return true;
    }
}
