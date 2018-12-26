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
            .fromOption { JFException("Cannot brokerTime instrument from asset name $assetName!") }

    fun <F> ContextDependencies<F>.forexInstrumentFromAssetName(assetName: String): Kind<F, Instrument> =
        bindingCatch {
            val instrument = fromAssetName(assetName).bind()
            if (instrument.type != IFinancialInstrument.Type.FOREX)
            {
                raiseError<JFException>(JFException("Asset $assetName is not a Forex instrument!"))
            }
            instrument
        }
}
