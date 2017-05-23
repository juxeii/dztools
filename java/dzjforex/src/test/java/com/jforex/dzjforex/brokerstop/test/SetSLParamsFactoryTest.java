package com.jforex.dzjforex.brokerstop.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.OfferSide;
import com.jforex.dzjforex.brokerstop.SetSLParamsFactory;
import com.jforex.dzjforex.order.StopLoss;
import com.jforex.dzjforex.test.util.CommonUtilForTest;
import com.jforex.programming.order.task.params.basic.SetSLParams;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class SetSLParamsFactoryTest extends CommonUtilForTest {

    private SetSLParamsFactory setSLParamsFactory;

    @Mock
    private StopLoss stopLossMock;
    private double slPrice;

    @Before
    public void setUp() {
        slPrice = brokerStopData.slPrice();

        setSLParamsFactory = new SetSLParamsFactory(stopLossMock, retryParamsMock);
    }

    private TestObserver<SetSLParams> subscribe() {
        return setSLParamsFactory
            .get(orderMockA, brokerStopData)
            .test();
    }

    private void setStopLossResult(final Single<Double> result) {
        when(stopLossMock.forPrice(instrumentForTest, slPrice))
            .thenReturn(result);
    }

    @Test
    public void getFailsWhenStopLossFails() {
        setStopLossResult(Single.error(jfException));

        subscribe().assertError(jfException);
    }

    public class OnStopLossSucceed {

        private SetSLParams setSLParams;

        @Before
        public void setUp() {
            setStopLossResult(Single.just(slPrice));

            setSLParams = (SetSLParams) subscribe()
                .getEvents()
                .get(0)
                .get(0);
        }

        @Test
        public void assertSetSLParamsValues() {
            assertThat(setSLParams.order(), equalTo(orderMockA));
            assertThat(setSLParams.offerSide(), equalTo(OfferSide.ASK));
            assertThat(setSLParams.priceOrPips(), equalTo(slPrice));
        }

        @Test
        public void assertComposeParams() throws Exception {
            assertComposeParamsForTask(setSLParams);
        }
    }
}
