package com.jforex.dzjforex.brokerbuy.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.stubbing.OngoingStubbing;

import com.dukascopy.api.IEngine.OrderCommand;
import com.jforex.dzjforex.brokerbuy.BrokerBuyData;
import com.jforex.dzjforex.brokerbuy.SubmitParamsFactory;
import com.jforex.dzjforex.order.StopLoss;
import com.jforex.dzjforex.testutil.CommonUtilForTest;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.task.params.basic.SubmitParams;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class SubmitParamsFactoryTest extends CommonUtilForTest {

    private SubmitParamsFactory submitParamsFactory;

    @Mock
    private StopLoss stopLossMock;
    @Mock
    private BrokerBuyData brokerBuyDataMock;
    private final double slPrice = 1.12345;
    private final double amount = 0.124;
    private final OrderCommand orderCommand = OrderCommand.BUY;

    @Before
    public void setUp() {
        setUpMocks();

        submitParamsFactory = new SubmitParamsFactory(retryParamsMock, stopLossMock);
    }

    private void setUpMocks() {
        when(brokerBuyDataMock.assetName()).thenReturn(instrumentNameForTest);
        when(brokerBuyDataMock.orderCommand()).thenReturn(orderCommand);
        when(brokerBuyDataMock.amount()).thenReturn(amount);
        when(brokerBuyDataMock.orderLabel()).thenReturn(orderLabel);
        when(brokerBuyDataMock.orderID()).thenReturn(orderID);
    }

    private OngoingStubbing<Single<Double>> stubSetSLResult() {
        return when(stopLossMock.forSubmit(instrumentForTest, brokerBuyDataMock));
    }

    private TestObserver<SubmitParams> subscribe() {
        return submitParamsFactory
            .get(instrumentForTest, brokerBuyDataMock)
            .test();
    }

    @Test
    public void getCallIsDeferred() {
        submitParamsFactory.get(instrumentForTest, brokerBuyDataMock);

        verifyZeroInteractions(tradeUtilityMock);
        verifyZeroInteractions(stopLossMock);
        verifyZeroInteractions(brokerBuyDataMock);
    }

    @Test
    public void getFailsWhenStopLossFails() {
        stubSetSLResult().thenReturn(Single.error(jfException));

        subscribe().assertError(jfException);
    }

    public class OnStopLossSucceed {

        private SubmitParams submitParams;
        private OrderParams orderParams;

        @Before
        public void setUp() {
            stubSetSLResult().thenReturn(Single.just(slPrice));

            submitParams = (SubmitParams) subscribe()
                .getEvents()
                .get(0)
                .get(0);
            orderParams = submitParams.orderParams();
        }

        @Test
        public void assertOrderParamsValues() {
            assertThat(orderParams.instrument(), equalTo(instrumentForTest));
            assertThat(orderParams.orderCommand(), equalTo(OrderCommand.BUY));
            assertThat(orderParams.amount(), equalTo(amount));
            assertThat(orderParams.label(), equalTo(orderLabel));
            assertThat(orderParams.stopLossPrice(), equalTo(slPrice));
        }

        @Test
        public void assertComposeParams() throws Exception {
            assertComposeParamsForTask(submitParams);
        }
    }
}
