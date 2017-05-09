package com.jforex.dzjforex.misc;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.programming.instrument.InstrumentFactory;
import com.jforex.programming.order.task.params.RetryParams;
import com.jforex.programming.rx.RetryDelay;
import com.jforex.programming.rx.RetryWhenFunctionForSingle;
import com.jforex.programming.rx.RxUtil;

import io.reactivex.Maybe;

public class RxUtility {

    public static <T> Maybe<T> optionalToMaybe(final Optional<T> maybeT) {
        return maybeT.isPresent()
                ? Maybe.just(maybeT.get())
                : Maybe.empty();
    }

    public static Maybe<Instrument> maybeInstrumentFromName(final String assetName) {
        final Optional<Instrument> maybeInstrument = InstrumentFactory.maybeFromName(assetName);
        return optionalToMaybe(maybeInstrument);
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
