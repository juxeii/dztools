package com.jforex.dzjforex.order;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.programming.instrument.InstrumentUtil;
import com.jforex.programming.math.MathUtil;
import com.jforex.programming.strategy.StrategyUtil;

public class StopLoss {

    private final StrategyUtil strategyUtil;
    private final PluginConfig pluginConfig;

    private final static double noStopLossDistance = 0.0;
    private final static double oppositeClose = -1;
    private final static Logger logger = LogManager.getLogger(StopLoss.class);

    public StopLoss(final StrategyUtil strategyUtil,
                    final PluginConfig pluginConfig) {
        this.strategyUtil = strategyUtil;
        this.pluginConfig = pluginConfig;
    }

    public double calculate(final Instrument instrument,
                            final OrderCommand orderCommand,
                            final double dStopDist) {
        return isDStopDistValid(dStopDist, instrument)
                ? getSL(instrument,
                        orderCommand,
                        dStopDist)
                : StrategyUtil.platformSettings.noSLPrice();
    }

    private boolean isDStopDistValid(final double dStopDist,
                                     final Instrument instrument) {
        if (dStopDist == noStopLossDistance
                || dStopDist == oppositeClose
                || !isDistanceOK(instrument, dStopDist))
            return false;
        return true;
    }

    private double getSL(final Instrument instrument,
                         final OrderCommand orderCommand,
                         final double dStopDist) {
        final double currentAskPrice = currentAsk(instrument);
        final double spread = strategyUtil
            .instrumentUtil(instrument)
            .spread();
        final double rawSLPrice = orderCommand == OrderCommand.BUY
                ? currentAskPrice - dStopDist - spread
                : currentAskPrice + dStopDist + spread;
        final double slPrice = MathUtil.roundPrice(rawSLPrice, instrument);
        logger.debug("Calculating SL price for " + instrument + ":\n"
                + " orderCommand: " + orderCommand + "\n"
                + " dStopDist: " + dStopDist + "\n"
                + " currentAskPrice: " + currentAskPrice + "\n"
                + " spread: " + spread + "\n"
                + " slPrice: " + slPrice);

        return slPrice;
    }

    public boolean isDistanceOK(final Instrument instrument,
                                final double stopDistance) {
        if (stopDistance == 0.0 || stopDistance == -1)
            return true;

        final double stopDistanceInPips = InstrumentUtil.scalePriceToPips(instrument, stopDistance);
        return stopDistanceInPips >= pluginConfig.minPipsForSL();
    }

    public boolean isPriceOK(final Instrument instrument,
                             final double newSL) {
        final double currentAskPrice = currentAsk(instrument);
        final double pipDistance = Math.abs(InstrumentUtil.pipDistanceOfPrices(instrument,
                                                                               currentAskPrice,
                                                                               newSL));
        return pipDistance >= pluginConfig.minPipsForSL();
    }

    private double currentAsk(final Instrument instrument) {
        return strategyUtil
            .instrumentUtil(instrument)
            .askQuote();
    }
}
