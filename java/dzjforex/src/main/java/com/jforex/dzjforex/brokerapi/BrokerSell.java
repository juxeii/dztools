package com.jforex.dzjforex.brokerapi;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.order.OrderActionResult;
import com.jforex.dzjforex.order.OrderClose;
import com.jforex.dzjforex.order.TradeUtil;

public class BrokerSell {

    private final TradeUtil tradeUtil;
    private final OrderClose orderClose;

    private final static Logger logger = LogManager.getLogger(BrokerSell.class);

    public BrokerSell(final TradeUtil tradeUtil,
                      final OrderClose orderClose) {
        this.tradeUtil = tradeUtil;
        this.orderClose = orderClose;
    }

    public int closeTrade(final int nTradeID,
                          final int nAmount) {
        final Optional<IOrder> maybeOrder = tradeUtil.maybeOrderForTrading(nTradeID);
        if (!maybeOrder.isPresent())
            return ZorroReturnValues.BROKER_SELL_FAIL.getValue();

        logger.info("Trying to close trade for nTradeID " + nTradeID
                + " and nAmount " + nAmount);
        return closeTradeForValidOrder(maybeOrder.get(), nAmount);
    }

    private int closeTradeForValidOrder(final IOrder order,
                                        final double nAmount) {
        final double amountToClose = tradeUtil.contractsToAmount(nAmount);
        final OrderActionResult closeResult = orderClose.run(order, amountToClose);

        return closeResult == OrderActionResult.FAIL
                ? ZorroReturnValues.BROKER_SELL_FAIL.getValue()
                : tradeUtil
                    .labelUtil()
                    .idFromOrder(order);
    }
}
