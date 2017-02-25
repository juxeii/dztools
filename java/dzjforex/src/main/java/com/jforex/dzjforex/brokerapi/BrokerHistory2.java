package com.jforex.dzjforex.brokerapi;

import java.util.Optional;

import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.handler.InstrumentRepository;
import com.jforex.dzjforex.history.BarFetcher;
import com.jforex.dzjforex.history.TickFetcher;

public class BrokerHistory2 {

    private final BarFetcher barFetcher;
    private final TickFetcher tickFetcher;

    public BrokerHistory2(final BarFetcher barFetcher,
                          final TickFetcher tickFetcher) {
        this.barFetcher = barFetcher;
        this.tickFetcher = tickFetcher;
    }

    public int get(final String assetName,
                   final double startDate,
                   final double endDate,
                   final int tickMinutes,
                   final int nTicks,
                   final double tickParams[]) {
        final Optional<Instrument> maybeInstrument = InstrumentRepository.maybeFromName(assetName);
        return maybeInstrument.isPresent()
                ? getForValidInstrument(maybeInstrument.get(),
                                        startDate,
                                        endDate,
                                        tickMinutes,
                                        nTicks,
                                        tickParams)
                : ZorroReturnValues.HISTORY_UNAVAILABLE.getValue();
    }

    private int getForValidInstrument(final Instrument instrument,
                                      final double startDate,
                                      final double endDate,
                                      final int tickMinutes,
                                      final int nTicks,
                                      final double tickParams[]) {
        return tickMinutes != 0
                ? barFetcher.fetch(instrument,
                                   startDate,
                                   endDate,
                                   tickMinutes,
                                   nTicks,
                                   tickParams)
                : tickFetcher.fetch(instrument,
                                    startDate,
                                    endDate,
                                    tickMinutes,
                                    nTicks,
                                    tickParams);
    }
}
