package com.jforex.dzjforex.handler;

import com.jforex.dzjforex.brokerapi.BrokerBuy;
import com.jforex.dzjforex.brokerapi.BrokerSell;
import com.jforex.dzjforex.brokerapi.BrokerStop;
import com.jforex.dzjforex.brokerapi.BrokerTrade;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.dzjforex.misc.InfoStrategy;
import com.jforex.dzjforex.order.HistoryOrders;
import com.jforex.dzjforex.order.OrderClose;
import com.jforex.dzjforex.order.OrderLabelUtil;
import com.jforex.dzjforex.order.OrderRepository;
import com.jforex.dzjforex.order.OrderSetLabel;
import com.jforex.dzjforex.order.OrderSetSL;
import com.jforex.dzjforex.order.OrderSubmit;
import com.jforex.dzjforex.order.RunningOrders;
import com.jforex.dzjforex.order.TradeUtil;

public class TradeHandler {

    private final BrokerTrade brokerTrade;
    private final BrokerBuy brokerBuy;
    private final BrokerSell brokerSell;
    private final BrokerStop brokerStop;

    public TradeHandler(final SystemHandler systemHandler,
                        final AccountHandler accountHandler,
                        final TimeHandler timeHandler,
                        final HistoryHandler historyHandler) {
        final PluginConfig pluginConfig = systemHandler.pluginConfig();
        final InfoStrategy infoStrategy = systemHandler.infoStrategy();

        final RunningOrders runningOrders = new RunningOrders(infoStrategy.getContext().getEngine());
        final HistoryOrders historyOrders = new HistoryOrders(historyHandler.historyProvider(),
                                                              accountHandler.brokerSubscribe(),
                                                              pluginConfig,
                                                              timeHandler.serverTimeProvider());
        final OrderLabelUtil orderLabelUtil = new OrderLabelUtil(pluginConfig, timeHandler.clock());
        final OrderRepository orderRepository = new OrderRepository(runningOrders,
                                                                    historyOrders,
                                                                    orderLabelUtil);
        final TradeUtil tradeUtil = new TradeUtil(orderRepository,
                                                  infoStrategy.strategyUtil(),
                                                  accountHandler.accountInfo(),
                                                  orderLabelUtil,
                                                  pluginConfig);
        final OrderSetSL setSLHandler = new OrderSetSL(tradeUtil);
        final OrderSubmit orderSubmit = new OrderSubmit(tradeUtil);
        final OrderSetLabel setLabel = new OrderSetLabel(tradeUtil);
        final OrderClose orderClose = new OrderClose(tradeUtil);

        brokerTrade = new BrokerTrade(tradeUtil);
        brokerBuy = new BrokerBuy(orderSubmit, tradeUtil);
        brokerSell = new BrokerSell(tradeUtil,
                                    orderClose,
                                    setLabel);
        brokerStop = new BrokerStop(setSLHandler, tradeUtil);
    }

    public int brokerTrade(final int orderID,
                           final double orderParams[]) {
        return brokerTrade.fillTradeParams(orderID, orderParams);
    }

    public int brokerBuy(final String instrumentName,
                         final double tradeParams[]) {
        return brokerBuy.openTrade(instrumentName, tradeParams);
    }

    public int brokerSell(final int nTradeID,
                          final int nAmount) {
        return brokerSell.closeTrade(nTradeID, nAmount);
    }

    public int brokerStop(final int orderID,
                          final double newSLPrice) {
        return brokerStop.setSL(orderID, newSLPrice);
    }
}
