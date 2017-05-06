package com.jforex.dzjforex.brokerbuy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.order.OrderActionResult;
import com.jforex.dzjforex.order.OrderLabelUtil;
import com.jforex.dzjforex.order.StopLoss;
import com.jforex.dzjforex.order.TaskParams;
import com.jforex.dzjforex.order.TradeUtility;
import com.jforex.programming.order.task.params.basic.SubmitParams;

import io.reactivex.Maybe;

public class OrderSubmit {

    private final TradeUtility tradeUtility;
    private final TaskParams taskParams;
    private final StopLoss stopLoss;
    private final OrderLabelUtil orderLabelUtil;

    private final static Logger logger = LogManager.getLogger(OrderSubmit.class);

    public OrderSubmit(final TradeUtility tradeUtility) {
        this.tradeUtility = tradeUtility;

        taskParams = tradeUtility.taskParams();
        stopLoss = tradeUtility.stopLoss();
        orderLabelUtil = tradeUtility.orderLabelUtil();
    }

    public int run(final Instrument instrument,
                   final BrokerBuyData brokerBuyData) {
        final String label = orderLabelUtil.create();
        final Maybe<SubmitParams> submitParams = submitParams(instrument,
                                                              brokerBuyData,
                                                              label);

        return submitParams
            .map(tradeUtility::runTaskParams)
            .map(submitResult -> brokerBuyData.stopDistance() != -1
                    ? evalSubmitResult(submitResult,
                                       brokerBuyData,
                                       label)
                    : ZorroReturnValues.BROKER_BUY_OPPOSITE_CLOSE.getValue())
            .defaultIfEmpty(ZorroReturnValues.BROKER_BUY_FAIL.getValue())
            .blockingGet();
    }

    private int evalSubmitResult(final OrderActionResult submitResult,
                                 final BrokerBuyData brokerBuyData,
                                 final String label) {
        return submitResult == OrderActionResult.FAIL
                ? ZorroReturnValues.BROKER_BUY_FAIL.getValue()
                : fillOpenPriceAndReturnOrderID(brokerBuyData, label);
    }

    private int fillOpenPriceAndReturnOrderID(final BrokerBuyData brokerBuyData,
                                              final String label) {
        final int orderID = orderLabelUtil.idFromLabel(label);
        final IOrder order = tradeUtility
            .maybeOrderByID(orderID)
            .blockingGet();
        brokerBuyData.fillOpenPrice(order.getOpenPrice());

        return orderID;
    }

    private Maybe<SubmitParams> submitParams(final Instrument instrument,
                                             final BrokerBuyData brokerBuyData,
                                             final String label) {
        final double dStopDist = brokerBuyData.stopDistance();
        if (!stopLoss.isDistanceOK(instrument, dStopDist)) {
            logger.error("The stop loss distance " + dStopDist + " is too small to open order!");
            return Maybe.empty();
        }

        final double contracts = brokerBuyData.contracts();
        final OrderCommand orderCommand = tradeUtility.orderCommandForContracts(contracts);
        final double amount = tradeUtility.contractsToAmount(contracts);
        final double slPrice = stopLoss.calculate(instrument,
                                                  orderCommand,
                                                  dStopDist);

        return Maybe.just(taskParams.forSubmit(instrument,
                                               orderCommand,
                                               amount,
                                               label,
                                               slPrice));
    }
}
