package com.jforex.dzjforex.misc;

import com.dukascopy.api.Instrument;
import com.jforex.programming.instrument.InstrumentUtil;
import com.jforex.programming.strategy.StrategyUtil;

public class PriceProvider {

    private final StrategyUtil strategyUtil;

    public PriceProvider(final StrategyUtil strategyUtil) {
        this.strategyUtil = strategyUtil;
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

    private InstrumentUtil instrumentUtil(final Instrument instrument) {
        return strategyUtil.instrumentUtil(instrument);
    }
}
