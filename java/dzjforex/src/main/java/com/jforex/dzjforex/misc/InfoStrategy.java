package com.jforex.dzjforex.misc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Period;
import com.jforex.programming.rx.JFHotPublisher;
import com.jforex.programming.strategy.JForexUtilsStrategy;
import com.jforex.programming.strategy.StrategyUtil;

import io.reactivex.Observable;

public class InfoStrategy extends JForexUtilsStrategy {

    private IContext context;
    private final JFHotPublisher<IMessage> messagePublisher = new JFHotPublisher<>();
    private final Observable<IMessage> orderMessages;

    private final static Logger logger = LogManager.getLogger(InfoStrategy.class);

    public InfoStrategy() {
        orderMessages = messagePublisher
            .observable()
            .filter(message -> message.getOrder() != null);
    }

    public IContext getContext() {
        return context;
    }

    public IAccount getAccount() {
        return context.getAccount();
    }

    public IHistory getHistory() {
        return context.getHistory();
    }

    public StrategyUtil strategyUtil() {
        return strategyUtil;
    }

    public Observable<IMessage> orderMessages() {
        return orderMessages;
    }

    @Override
    public void onJFAccount(final IAccount account) throws JFException {
    }

    @Override
    public void onJFBar(final Instrument instrument,
                        final Period period,
                        final IBar askBar,
                        final IBar bidBar) throws JFException {
    }

    @Override
    public void onJFMessage(final IMessage message) throws JFException {
        logger.info("Received message type " + message.getType());
        messagePublisher.onNext(message);
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
    }
}
