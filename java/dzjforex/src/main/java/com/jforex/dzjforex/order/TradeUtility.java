package com.jforex.dzjforex.order;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.jforex.dzjforex.brokeraccount.AccountInfo;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.dzjforex.misc.PriceProvider;
import com.jforex.dzjforex.misc.RxUtility;
import com.jforex.programming.order.task.params.RetryParams;

import io.reactivex.Maybe;
import io.reactivex.Single;

public class TradeUtility {

    private final OrderLookup orderRepository;
    private final PriceProvider priceProvider;
    private final OrderLabelUtil labelUtil;
    private final AccountInfo accountInfo;
    private final PluginConfig pluginConfig;
    private final RetryParams retryParams;

    private final static Logger logger = LogManager.getLogger(TradeUtility.class);

    public TradeUtility(final OrderLookup orderRepository,
                        final PriceProvider priceProvider,
                        final AccountInfo accountInfo,
                        final OrderLabelUtil orderLabel,
                        final RetryParams retryParams,
                        final PluginConfig pluginConfig) {
        this.orderRepository = orderRepository;
        this.priceProvider = priceProvider;
        this.accountInfo = accountInfo;
        this.labelUtil = orderLabel;
        this.retryParams = retryParams;
        this.pluginConfig = pluginConfig;
    }

    public OrderLabelUtil orderLabelUtil() {
        return labelUtil;
    }

    public RetryParams retryParams() {
        return retryParams;
    }

    public Single<Instrument> instrumentForTrading(final String assetName) {
        return isTradingAllowed()
                ? RxUtility.instrumentFromName(assetName)
                : Single.error(new JFException("Trading not allowed for asset " + assetName));
    }

    public Single<IOrder> orderForTrading(final int nTradeID) {
        return isTradingAllowed()
                ? orderByID(nTradeID).toSingle()
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

    public OrderCommand orderCommandForContracts(final int contracts) {
        return contracts > 0
                ? OrderCommand.BUY
                : OrderCommand.SELL;
    }

    public double contractsToAmount(final double contracts) {
        return Math.abs(contracts) / pluginConfig.lotScale();
    }

    public Maybe<IOrder> orderByID(final int nTradeID) {
        return orderRepository.getByID(nTradeID);
    }

    public double spread(final Instrument instrument) {
        return priceProvider.spread(instrument);
    }

    public double ask(final Instrument instrument) {
        return priceProvider.ask(instrument);
    }

    public double bid(final Instrument instrument) {
        return priceProvider.bid(instrument);
    }
}
