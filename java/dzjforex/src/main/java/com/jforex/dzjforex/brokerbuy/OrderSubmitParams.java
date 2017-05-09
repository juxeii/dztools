package com.jforex.dzjforex.brokerbuy;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.Instrument;
import com.jforex.dzjforex.order.StopLoss;
import com.jforex.dzjforex.order.TaskParams;
import com.jforex.dzjforex.order.TradeUtility;
import com.jforex.programming.order.task.params.basic.SubmitParams;
import com.jforex.programming.strategy.StrategyUtil;

import io.reactivex.Single;

public class OrderSubmitParams {

    private final TradeUtility tradeUtility;
    private final TaskParams taskParams;
    private final StopLoss stopLoss;

    public OrderSubmitParams(final TradeUtility tradeUtility,
                             final StopLoss stopLoss) {
        this.tradeUtility = tradeUtility;
        this.stopLoss = stopLoss;

        taskParams = tradeUtility.taskParams();
    }

    public Single<SubmitParams> get(final Instrument instrument,
                                    final BrokerBuyData brokerBuyData,
                                    final String label) {
        return Single.defer(() -> {
            final double contracts = brokerBuyData.contracts();
            final OrderCommand orderCommand = orderCommandForContracts(contracts);
            final double amount = tradeUtility.contractsToAmount(contracts);
            final double dStopDist = brokerBuyData.stopDistance();

            return stopLoss
                .forDistance(instrument,
                             orderCommand,
                             brokerBuyData.stopDistance())
                .defaultIfEmpty(StrategyUtil.platformSettings.noSLPrice())
                .toSingle()
                .map(slPrice -> taskParams.forSubmit(instrument,
                                                     orderCommand,
                                                     amount,
                                                     label,
                                                     slPrice));
        });
    }

    private OrderCommand orderCommandForContracts(final double contracts) {
        return contracts > 0
                ? OrderCommand.BUY
                : OrderCommand.SELL;
    }
}
