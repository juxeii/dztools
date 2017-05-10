package com.jforex.dzjforex.order;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.brokeraccount.AccountInfo;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.dzjforex.misc.RxUtility;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.task.params.TaskParamsWithType;
import com.jforex.programming.strategy.StrategyUtil;

import io.reactivex.Maybe;

public class TradeUtility {

    private final OrderRepository orderRepository;
    private final StrategyUtil strategyUtil;
    private final OrderUtil orderUtil;
    private final OrderLabelUtil labelUtil;
    private final AccountInfo accountInfo;
    private final PluginConfig pluginConfig;

    private final static Logger logger = LogManager.getLogger(TradeUtility.class);

    public TradeUtility(final OrderRepository orderRepository,
                        final StrategyUtil strategyUtil,
                        final AccountInfo accountInfo,
                        final OrderLabelUtil orderLabel,
                        final PluginConfig pluginConfig) {
        this.orderRepository = orderRepository;
        this.strategyUtil = strategyUtil;
        this.accountInfo = accountInfo;
        this.labelUtil = orderLabel;
        this.pluginConfig = pluginConfig;

        orderUtil = strategyUtil.orderUtil();
    }

    public OrderRepository orderRepository() {
        return orderRepository;
    }

    public StrategyUtil strategyUtil() {
        return strategyUtil;
    }

    public OrderUtil orderUtil() {
        return orderUtil;
    }

    public AccountInfo accountInfo() {
        return accountInfo;
    }

    public OrderLabelUtil orderLabelUtil() {
        return labelUtil;
    }

    public PluginConfig pluginConfig() {
        return pluginConfig;
    }

    public Maybe<Instrument> maybeInstrumentForTrading(final String assetName) {
        return isTradingAllowed()
                ? RxUtility.maybeInstrumentFromName(assetName)
                : Maybe.empty();
    }

    public Maybe<IOrder> maybeOrderForTrading(final int nTradeID) {
        return isTradingAllowed()
                ? maybeOrderByID(nTradeID)
                : Maybe.empty();
    }

    public boolean isTradingAllowed() {
        if (!accountInfo.isTradingAllowed()) {
            logger.warn("Trading account is not available, since it is in state " + accountInfo.state());
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

    public Maybe<IOrder> maybeOrderByID(final int nTradeID) {
        return orderRepository.maybeOrderByID(nTradeID);
    }

    public OrderActionResult runTaskParams(final TaskParamsWithType taskParams) {
        final Throwable resultError = orderUtil
            .paramsToObservable(taskParams)
            .ignoreElements()
            .blockingGet();

        return resultError == null
                ? OrderActionResult.OK
                : OrderActionResult.FAIL;
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
}
