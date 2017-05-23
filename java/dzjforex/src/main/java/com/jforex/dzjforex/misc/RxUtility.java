package com.jforex.dzjforex.misc;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.programming.instrument.InstrumentFactory;
import com.jforex.programming.order.task.params.RetryParams;
import com.jforex.programming.rx.RetryDelay;
import com.jforex.programming.rx.RetryWhenFunctionForSingle;
import com.jforex.programming.rx.RxUtil;

import io.reactivex.Single;

public class RxUtility {

    private RxUtility() {
    }

    public static Single<Instrument> instrumentFromName(final String assetName) {
        final Optional<Instrument> maybeInstrument = InstrumentFactory.maybeFromName(assetName);
        return maybeInstrument.isPresent()
                ? Single.just(maybeInstrument.get())
                : Single.error(new JFException("Asset name " + assetName + " is invalid!"));
    }

    public static RetryParams retryParams(final int retries,
                                          final long delay) {
        final RetryDelay retryDelay = new RetryDelay(delay, TimeUnit.MILLISECONDS);
        return new RetryParams(retries, att -> retryDelay);
    }

    public static RetryParams retryParamsForHistory(final PluginConfig pluginConfig) {
        return retryParams(pluginConfig.historyAccessRetries(), pluginConfig.historyAccessRetryDelay());
    }

    public static RetryWhenFunctionForSingle retryForHistory(final PluginConfig pluginConfig) {
        return RxUtil.retryWithDelayForSingle(retryParamsForHistory(pluginConfig));
    }
}
