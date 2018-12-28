package com.jforex.dzjforex.misc

import arrow.Kind
import com.dukascopy.api.Instrument
import com.dukascopy.api.JFException
import com.jforex.kforexutils.instrument.InstrumentFactory

object InstrumentApi {
    fun <F> ContextDependencies<F>.fromAssetName(assetName: String): Kind<F, Instrument> =
        InstrumentFactory
            .fromName(assetName)
            .fromOption { JFException("Asset name $assetName is not a valid instrument!") }

    fun <F> ContextDependencies<F>.isTradeable(instrument: Instrument): Kind<F, Boolean> =
        invoke { context.engine.isTradable(instrument) }
}
