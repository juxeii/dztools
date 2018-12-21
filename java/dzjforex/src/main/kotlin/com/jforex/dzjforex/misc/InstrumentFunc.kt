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

interface InstrumentFunc<F> : MonadError<F, Throwable>
{
    companion object
    {
        operator fun <F> invoke(ME: MonadError<F, Throwable>): InstrumentFunc<F> =
            object : InstrumentFunc<F>, MonadError<F, Throwable> by ME
            {}
    }
}

object InstrumentApi
{
    fun <F> InstrumentFunc<F>.fromAssetName(assetName: String): Kind<F, Instrument> =
        InstrumentFactory
            .fromName(assetName)
            .fromOption { JFException("Cannot create instrument from asset name $assetName!") }

    fun <F> InstrumentFunc<F>.forexInstrumentFromAssetName(assetName: String): Kind<F, Instrument> =
        bindingCatch {
            val instrument = fromAssetName(assetName).bind()
            if (instrument.type != IFinancialInstrument.Type.FOREX)
            {
                raiseError<JFException>(JFException("Asset $assetName is not a Forex instrument!"))
            }
            instrument
        }
}
