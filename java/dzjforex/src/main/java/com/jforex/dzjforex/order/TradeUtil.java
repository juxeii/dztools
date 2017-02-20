package com.jforex.dzjforex.order;

import java.rmi.server.UID;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.dzjforex.handler.AccountInfo;
import com.jforex.dzjforex.handler.InstrumentHandler;
import com.jforex.programming.instrument.InstrumentUtil;
import com.jforex.programming.math.MathUtil;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.strategy.StrategyUtil;

public class TradeUtil {

    private final OrderRepository orderRepository;
    private final StrategyUtil strategyUtil;
    private final OrderUtil orderUtil;
    private final AccountInfo accountInfo;
    private final PluginConfig pluginConfig;

    private final static Logger logger = LogManager.getLogger(TradeUtil.class);

    public TradeUtil(final OrderRepository orderRepository,
                     final StrategyUtil strategyUtil,
                     final AccountInfo accountInfo,
                     final PluginConfig pluginConfig) {
        this.orderRepository = orderRepository;
        this.strategyUtil = strategyUtil;
        this.accountInfo = accountInfo;
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

    public PluginConfig pluginConfig() {
        return pluginConfig;
    }

    public void storeOrder(final int orderID,
                           final IOrder order) {
        orderRepository.storeOrder(orderID, order);
    }

    public boolean isOrderIDKnown(final int orderID) {
        return orderRepository.orderByID(orderID) != null;
    }

    public IOrder getOrder(final int orderID) {
        return orderRepository.orderByID(orderID);
    }

    public String orderLabelPrefix() {
        return pluginConfig.orderLabelPrefix();
    }

    public Optional<Instrument> maybeInstrumentForTrading(final String assetName) {
        return isTradingAllowed()
                ? InstrumentHandler.fromName(assetName)
                : Optional.empty();
    }

    public boolean isTradingAllowed() {
        if (!accountInfo.isTradingAllowed()) {
            logger.warn("Trading account is not available, since it is in state " + accountInfo.state());
            return false;
        }
        return true;
    }

    public double calculateSL(final Instrument instrument,
                              final OrderCommand orderCommand,
                              final double dStopDist) {
        if (dStopDist == 0.0 || dStopDist == -1)
            return StrategyUtil.platformSettings.noSLPrice();

        final InstrumentUtil instrumentUtil = strategyUtil.instrumentUtil(instrument);
        final double currentAskPrice = instrumentUtil.askQuote();
        final double spread = instrumentUtil.spread();

        final double rawSLPrice = orderCommand == OrderCommand.BUY
                ? currentAskPrice - dStopDist - spread
                : currentAskPrice + dStopDist + spread;
        final double slPrice = MathUtil.roundPrice(rawSLPrice, instrument);
        logger.debug("Calculating SL price for " + instrument + "\n"
                + " orderCommand " + orderCommand + "\n"
                + " dStopDist " + dStopDist + "\n"
                + " currentAskPrice " + currentAskPrice + "\n"
                + " spread " + spread + "\n"
                + " slPrice " + slPrice);

        if (!isSLPriceDistanceOK(instrument, slPrice))
            return StrategyUtil.platformSettings.noSLPrice();
        return slPrice;
    }

    public boolean isSLPriceDistanceOK(final Instrument instrument,
                                       final double newSL) {
        final double currentAskPrice = strategyUtil
            .instrumentUtil(instrument)
            .askQuote();

        final double pipDistance = Math.abs(InstrumentUtil.pipDistanceOfPrices(instrument,
                                                                               currentAskPrice,
                                                                               newSL));
        if (pipDistance < pluginConfig.minPipsForSL()) {
            logger.error("Cannot set SL price to " + newSL
                    + " since the pip distance " + pipDistance
                    + " is too small! Minimum pips: " + pluginConfig.minPipsForSL());
            return false;
        }
        return true;
    }

    public OrderCommand orderCommandForContracts(final double contracts) {
        return contracts > 0
                ? OrderCommand.BUY
                : OrderCommand.SELL;
    }

    public int scaleAmount(final double amount) {
        return (int) (amount * pluginConfig.lotScale());
    }

    public double contractsToAmount(final double contracts) {
        return Math.abs(contracts) / pluginConfig.lotScale();
    }

    public String createLabel() {
        return orderLabelPrefix() + Math.abs(new UID().hashCode());
    }
}
