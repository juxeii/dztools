package com.jforex.dzjforex.misc

import arrow.Kind
import arrow.core.Failure
import arrow.core.Try
import arrow.instances.`try`.monadError.monadError
import arrow.typeclasses.MonadError
import arrow.typeclasses.bindingCatch
import com.dukascopy.api.Instrument
import com.dukascopy.api.JFException
import com.dukascopy.api.instrument.IFinancialInstrument
import com.jforex.kforexutils.instrument.InstrumentFactory

object InstrumentApi
{
    fun <F> ContextDependencies<F>.fromAssetName(assetName: String): Kind<F, Instrument> =
        InstrumentFactory
            .fromName(assetName)
            .fromOption { JFException("Asset name $assetName is not a valid instrument!") }
}
