package com.jforex.dzjforex.misc

import arrow.Kind
import com.dukascopy.api.Instrument
import com.dukascopy.api.JFException
import com.jforex.kforexutils.instrument.InstrumentFactory

object InstrumentApi
{
    fun <F> ContextDependencies<F>.createInstrument(assetName: String): Kind<F, Instrument> =
        InstrumentFactory
            .fromName(assetName)
            .fromOption { JFException("Asset name $assetName is not a valid instrument!") }

    fun <F> ContextDependencies<F>.filterTradeableInstrument(instrument: Instrument): Kind<F, Instrument> =
        delay {
            if (!instrument.isTradable) throw AssetNotTradeableException(instrument)
            instrument
        }
}
