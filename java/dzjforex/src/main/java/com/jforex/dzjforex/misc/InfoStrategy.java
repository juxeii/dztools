package com.jforex.dzjforex.misc;

import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Period;
import com.jforex.programming.strategy.JForexUtilsStrategy;
import com.jforex.programming.strategy.StrategyUtil;

public class InfoStrategy extends JForexUtilsStrategy {

    private IContext context;
    private ITick latestTick;

    public IContext getContext() {
        return context;
    }

    public StrategyUtil strategyUtil() {
        return strategyUtil;
    }

    public ITick getLatestTick() {
        return latestTick;
    }

    @Override
    public void onJFAccount(final IAccount arg0) throws JFException {
    }

    @Override
    public void onJFBar(final Instrument arg0,
                        final Period arg1,
                        final IBar arg2,
                        final IBar arg3) throws JFException {
    }

    @Override
    public void onJFMessage(final IMessage arg0) throws JFException {
    }

    @Override
    public void onJFStart(final IContext context) throws JFException {
        this.context = context;
    }

    @Override
    public void onJFStop() throws JFException {
    }

    @Override
    public void onJFTick(final Instrument instrument,
                         final ITick tick) throws JFException {
        latestTick = tick;
    }
}
