package com.jforex.dzjforex.brokerapi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.jforex.dzjforex.config.ZorroReturnValues;
import com.jforex.dzjforex.order.OrderActionResult;
import com.jforex.dzjforex.order.TradeUtility;
import com.jforex.programming.order.task.params.basic.CloseParams;

import io.reactivex.Single;

public class BrokerSell {

    private final TradeUtility tradeUtility;

    private final static Logger logger = LogManager.getLogger(BrokerSell.class);

    public BrokerSell(final TradeUtility tradeUtility) {
        this.tradeUtility = tradeUtility;
    }

    public int closeTrade(final BrokerSellData brokerSellData) {
        final int nTradeID = brokerSellData.nTradeID();
        final int nAmount = brokerSellData.nAmount();

        return tradeUtility
            .maybeOrderForTrading(nTradeID)
            .map(order -> closeTradeForValidOrder(order,
                                                  nTradeID,
                                                  nAmount))
            .defaultIfEmpty(ZorroReturnValues.BROKER_SELL_FAIL.getValue())
            .blockingGet();
    }

    private int closeTradeForValidOrder(final IOrder order,
                                        final int nTradeID,
                                        final int nAmount) {
        return Single
            .just(tradeUtility.contractsToAmount(nAmount))
            .doOnSubscribe(d -> logger.info("Trying to close trade for nTradeID " + nTradeID
                    + " and nAmount " + nAmount))
            .map(amount -> closeParamsFromAmount(order, amount))
            .map(closeParams -> tradeUtility.runTaskParams(closeParams))
            .map(closeResult -> evalCloseResult(closeResult, nTradeID))
            .blockingGet();
    }

    private CloseParams closeParamsFromAmount(final IOrder order,
                                              final double amountToClose) {
        return tradeUtility
            .taskParams()
            .forClose(order, amountToClose);
    }

    private int evalCloseResult(final OrderActionResult closeResult,
                                final int nTradeID) {
        return closeResult == OrderActionResult.FAIL
                ? ZorroReturnValues.BROKER_SELL_FAIL.getValue()
                : nTradeID;
    }
}
