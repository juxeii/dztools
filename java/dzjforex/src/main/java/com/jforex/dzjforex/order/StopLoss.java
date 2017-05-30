package com.jforex.dzjforex.order;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.jforex.dzjforex.brokerbuy.BrokerBuyData;
import com.jforex.programming.instrument.InstrumentUtil;
import com.jforex.programming.math.CalculationUtil;
import com.jforex.programming.math.MathUtil;
import com.jforex.programming.strategy.StrategyUtil;

import io.reactivex.Single;

public class StopLoss {

    private final CalculationUtil calculationUtil;
    private final double minPipsForSL;

    private final static double noStopLossDistance = 0.0;
    private final static double oppositeClose = -1;
    private final static Logger logger = LogManager.getLogger(StopLoss.class);

    public StopLoss(final CalculationUtil calculationUtil,
                    final double minPipsForSL) {
        this.calculationUtil = calculationUtil;
        this.minPipsForSL = minPipsForSL;
    }

    public Single<Double> forSubmit(final Instrument instrument,
                                    final BrokerBuyData brokerBuyData) {
        return Single
            .fromCallable(brokerBuyData::slDistance)
            .flatMap(slDistance -> isDistanceForNoSL(slDistance)
                    ? Single.just(StrategyUtil.platformSettings.noSLPrice())
                    : forSubmitWithRealDistance(instrument, brokerBuyData));
    }

    public Single<Double> forSubmitWithRealDistance(final Instrument instrument,
                                                    final BrokerBuyData brokerBuyData) {
        return Single
            .just(brokerBuyData.slDistance())
            .map(slDistance -> InstrumentUtil.scalePriceToPips(instrument, slDistance))
            .flatMap(this::checkPipDistance)
            .map(slDistance -> slPriceForPips(instrument,
                                              brokerBuyData.orderCommand(),
                                              slDistance));
    }

    private boolean isDistanceForNoSL(final double slDistance) {
        return slDistance == noStopLossDistance || slDistance == oppositeClose;
    }

    private double slPriceForPips(final Instrument instrument,
                                  final OrderCommand orderCommand,
                                  final double slDistance) {
        final double rawSLPrice = calculationUtil.slPriceForPips(instrument,
                                                                 orderCommand,
                                                                 slDistance);
        final double slPrice = MathUtil.roundPrice(rawSLPrice, instrument);
        logger.debug("Calculated SL price for " + instrument + ":\n"
                + " orderCommand: " + orderCommand + "\n"
                + " slDistance: " + slDistance + "\n"
                + " slPrice: " + slPrice);

        return slPrice;
    }

    public Single<Double> forSetSL(final IOrder order,
                                   final double slPrice) {
        return Single
            .fromCallable(() -> pipDistanceOfPrices(order, slPrice))
            .flatMap(this::checkPipDistance)
            .map(slDistance -> MathUtil.roundPrice(slPrice, order.getInstrument()));

    }

    private double pipDistanceOfPrices(final IOrder order,
                                       final double slPrice) {
        final Instrument instrument = order.getInstrument();
        final double currentPrice = calculationUtil.currentQuoteForOrderCommand(instrument,
                                                                                order.getOrderCommand());
        return InstrumentUtil.pipDistanceOfPrices(instrument,
                                                  currentPrice,
                                                  slPrice);
    }

    private Single<Double> checkPipDistance(final double slDistance) {
        return Math.abs(slDistance) >= minPipsForSL
                ? Single.just(slDistance)
                : Single.error(new JFException("The stop loss slDistance " + slDistance
                        + " is too small! Minimum pip distance is " + minPipsForSL));
    }
}
