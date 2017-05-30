package com.jforex.dzjforex.brokerstop.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.stubbing.OngoingStubbing;

import com.dukascopy.api.OfferSide;
import com.jforex.dzjforex.brokerstop.SetSLParamsFactory;
import com.jforex.dzjforex.order.StopLoss;
import com.jforex.dzjforex.testutil.CommonUtilForTest;
import com.jforex.programming.order.task.params.basic.SetSLParams;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class SetSLParamsFactoryTest extends CommonUtilForTest {

    private SetSLParamsFactory setSLParamsFactory;

    @Mock
    private StopLoss stopLossMock;

    @Before
    public void setUp() {
        setSLParamsFactory = new SetSLParamsFactory(stopLossMock, retryParamsMock);
    }

    private TestObserver<SetSLParams> subscribe() {
        return setSLParamsFactory
            .get(orderMockA, brokerStopDataMock)
            .test();
    }

    private OngoingStubbing<Single<Double>> stubSetSLResult() {
        return when(stopLossMock.forSetSL(orderMockA, slPrice));
    }

    @Test
    public void getCallIsDeferred() {
        setSLParamsFactory.get(orderMockA, brokerStopDataMock);

        verifyZeroInteractions(brokerStopDataMock);
        verifyZeroInteractions(stopLossMock);
        verifyZeroInteractions(retryParamsMock);
    }

    @Test
    public void getFailsWhenStopLossFails() {
        stubSetSLResult().thenReturn(Single.error(jfException));

        subscribe().assertError(jfException);
    }

    public class OnStopLossSucceed {

        private SetSLParams setSLParams;

        @Before
        public void setUp() {
            stubSetSLResult().thenReturn(Single.just(slPrice));

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
