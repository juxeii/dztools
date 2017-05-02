package com.jforex.dzjforex.brokerapi;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.order.OrderSetSL;
import com.jforex.dzjforex.order.TradeUtil;
import com.jforex.programming.math.MathUtil;

public class BrokerStop {

    private final OrderSetSL orderSetSL;
    private final TradeUtil tradeUtil;

    private final static Logger logger = LogManager.getLogger(BrokerStop.class);

    public BrokerStop(final OrderSetSL orderSetSL,
                      final TradeUtil tradeUtil) {
        this.orderSetSL = orderSetSL;
        this.tradeUtil = tradeUtil;
    }

    public int setSL(final int nTradeID,
                     final double dStop) {
        final Optional<IOrder> maybeOrder = tradeUtil.maybeOrderForTrading(nTradeID);
        if (!maybeOrder.isPresent())
            return ZorroReturnValues.ADJUST_SL_FAIL.getValue();

        logger.info("Trying to set stop loss for order ID " + nTradeID
                + " and dStop " + dStop);
        return setSLForValidOrderID(maybeOrder.get(), dStop);
    }

    private int setSLForValidOrderID(final IOrder order,
                                     final double dStop) {
        final double slPrice = MathUtil.roundPrice(dStop, order.getInstrument());
        if (tradeUtil.isSLPriceDistanceOK(order.getInstrument(), slPrice))
            orderSetSL.run(order, slPrice);

        return ZorroReturnValues.ADJUST_SL_OK.getValue();
    }
}
