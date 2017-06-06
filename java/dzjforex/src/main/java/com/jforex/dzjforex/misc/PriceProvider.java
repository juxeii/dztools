package com.jforex.dzjforex.misc;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.instrument.InstrumentUtil;
import com.jforex.programming.math.CalculationUtil;
import com.jforex.programming.strategy.StrategyUtil;

public class PriceProvider {

    private final StrategyUtil strategyUtil;
    private final CalculationUtil calculationUtil;

    public PriceProvider(final StrategyUtil strategyUtil) {
        this.strategyUtil = strategyUtil;

        calculationUtil = strategyUtil.calculationUtil();
    }

    public double ask(final Instrument instrument) {
        return instrumentUtil(instrument).askQuote();
    }

    public double bid(final Instrument instrument) {
        return instrumentUtil(instrument).bidQuote();
    }

    public double spread(final Instrument instrument) {
        return instrumentUtil(instrument).spread();
    }

    public double forOrder(final IOrder order) {
        final Instrument instrument = order.getInstrument();
        final OrderCommand orderCommand = order.getOrderCommand();
        return calculationUtil.currentQuoteForOrderCommand(instrument, orderCommand);
    }

    private InstrumentUtil instrumentUtil(final Instrument instrument) {
        return strategyUtil.instrumentUtil(instrument);
    }
}
