package com.jforex.dzjforex.order;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.jforex.programming.instrument.InstrumentUtil;
import com.jforex.programming.math.MathUtil;

import io.reactivex.Maybe;
import io.reactivex.Single;

public class StopLoss {

    private final TradeUtility tradeUtility;
    private final double minPipsForSL;

    private final static double noStopLossDistance = 0.0;
    private final static double oppositeClose = -1;
    private final static Logger logger = LogManager.getLogger(StopLoss.class);

    public StopLoss(final TradeUtility tradeUtility,
                    final double minPipsForSL) {
        this.tradeUtility = tradeUtility;
        this.minPipsForSL = minPipsForSL;
    }

    public Maybe<Double> forDistance(final Instrument instrument,
                                     final OrderCommand orderCommand,
                                     final double dStopDist) {
        return Maybe.defer(() -> {
            if (!isDistanceOK(instrument, dStopDist))
                return Maybe.error(new JFException("The stop loss distance " + dStopDist
                        + " is too small for " + instrument
                        + "! Minimum pip distance is " + minPipsForSL));
            if (isDistanceForNoSL(dStopDist))
                return Maybe.empty();

            return Maybe.just(calculate(instrument,
                                        orderCommand,
                                        dStopDist));
        });
    }

    private double calculate(final Instrument instrument,
                             final OrderCommand orderCommand,
                             final double dStopDist) {
        final double currentAskPrice = tradeUtility.ask(instrument);
        final double spread = tradeUtility.spread(instrument);
        final double rawSLPrice = orderCommand == OrderCommand.BUY
                ? currentAskPrice - dStopDist - spread
                : currentAskPrice + dStopDist + spread;
        final double slPrice = MathUtil.roundPrice(rawSLPrice, instrument);
        logger.debug("Calculated SL price for " + instrument + ":\n"
                + " orderCommand: " + orderCommand + "\n"
                + " dStopDist: " + dStopDist + "\n"
                + " currentAskPrice: " + currentAskPrice + "\n"
                + " spread: " + spread + "\n"
                + " slPrice: " + slPrice);

        return slPrice;
    }

    private boolean isDistanceOK(final Instrument instrument,
                                 final double dStopDist) {
        if (isDistanceForNoSL(dStopDist))
            return true;

        final double stopDistanceInPips = InstrumentUtil.scalePriceToPips(instrument, dStopDist);
        return stopDistanceInPips >= minPipsForSL;
    }

    private boolean isDistanceForNoSL(final double dStopDist) {
        return dStopDist == noStopLossDistance || dStopDist == oppositeClose;
    }

    public Single<Double> forPrice(final Instrument instrument,
                                   final double slPrice) {
        final double currentAskPrice = tradeUtility.ask(instrument);
        final double pipDistance = InstrumentUtil.pipDistanceOfPrices(instrument,
                                                                      currentAskPrice,
                                                                      slPrice);
        return Math.abs(pipDistance) >= minPipsForSL
                ? Single.just(slPrice)
                : Single.error(new JFException("The stop loss price " + slPrice
                        + " is too close to current ask " + currentAskPrice
                        + " for " + instrument
                        + "! Minimum pip distance is " + minPipsForSL));
    }
}
