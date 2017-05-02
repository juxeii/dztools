package com.jforex.dzjforex.handler;

import com.jforex.dzjforex.brokerapi.BrokerBuy;
import com.jforex.dzjforex.brokerapi.BrokerBuyData;
import com.jforex.dzjforex.brokerapi.BrokerSell;
import com.jforex.dzjforex.brokerapi.BrokerSellData;
import com.jforex.dzjforex.brokerapi.BrokerStop;
import com.jforex.dzjforex.brokerapi.BrokerStopData;
import com.jforex.dzjforex.brokerapi.BrokerTrade;
import com.jforex.dzjforex.brokerapi.BrokerTradeData;
import com.jforex.dzjforex.config.PluginConfig;
import com.jforex.dzjforex.misc.InfoStrategy;
import com.jforex.dzjforex.order.HistoryOrders;
import com.jforex.dzjforex.order.OrderLabelUtil;
import com.jforex.dzjforex.order.OrderRepository;
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
        brokerTrade = new BrokerTrade(tradeUtil);
        brokerBuy = new BrokerBuy(tradeUtil);
        brokerSell = new BrokerSell(tradeUtil);
        brokerStop = new BrokerStop(tradeUtil);
    }

    public int brokerTrade(final BrokerTradeData brokerTradeData) {
        return brokerTrade.handle(brokerTradeData);
    }

    public int brokerBuy(final BrokerBuyData brokerBuyData) {
        return brokerBuy.openTrade(brokerBuyData);
    }

    public int brokerSell(final BrokerSellData brokerSellData) {
        return brokerSell.closeTrade(brokerSellData);
    }

    public int brokerStop(final BrokerStopData brokerStopData) {
        return brokerStop.setSL(brokerStopData);
    }
}
