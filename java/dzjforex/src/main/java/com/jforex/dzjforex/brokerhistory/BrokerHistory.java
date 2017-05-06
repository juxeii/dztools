package com.jforex.dzjforex.brokerhistory;

import java.util.Optional;

import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.programming.instrument.InstrumentFactory;

public class BrokerHistory {

    private final BarFetcher barFetcher;
    private final TickFetcher tickFetcher;

    public BrokerHistory(final BarFetcher barFetcher,
                         final TickFetcher tickFetcher) {
        this.barFetcher = barFetcher;
        this.tickFetcher = tickFetcher;
    }

    public int get(final BrokerHistoryData brokerHistoryData) {
        final Optional<Instrument> maybeInstrument =
                InstrumentFactory.maybeFromName(brokerHistoryData.instrumentName());
        return maybeInstrument.isPresent()
                ? getForValidInstrument(maybeInstrument.get(), brokerHistoryData)
                : ZorroReturnValues.HISTORY_UNAVAILABLE.getValue();
    }

    private int getForValidInstrument(final Instrument instrument,
                                      final BrokerHistoryData brokerHistoryData) {
        return brokerHistoryData.noOfTickMinutes() != 0
                ? barFetcher.run(instrument, brokerHistoryData)
                : tickFetcher.run(instrument, brokerHistoryData);
    }
}
