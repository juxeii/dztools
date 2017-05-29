package com.jforex.dzjforex.brokerbuy.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.stubbing.OngoingStubbing;

import com.dukascopy.api.IOrder;
import com.jforex.dzjforex.brokerbuy.BrokerBuyData;
import com.jforex.dzjforex.brokerbuy.SubmitParamsFactory;
import com.jforex.dzjforex.brokerbuy.SubmitParamsRunner;
import com.jforex.dzjforex.testutil.CommonUtilForTest;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.params.basic.SubmitParams;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class SubmitParamsRunnerTest extends CommonUtilForTest {

    private SubmitParamsRunner submitParamsRunner;

    @Mock
    private SubmitParamsFactory submitParamsFactoryMock;
    @Mock
    private SubmitParams submitParamsMock;
    @Mock
    private BrokerBuyData brokerBuyDataMock;

    @Before
    public void setUp() {
        submitParamsRunner = new SubmitParamsRunner(orderUtilMock, submitParamsFactoryMock);
    }

    private TestObserver<IOrder> subscribe() {
        return submitParamsRunner
            .get(instrumentForTest, brokerBuyDataMock)
            .test();
    }

    private OngoingStubbing<Observable<OrderEvent>> stubParamsToObservable() {
        return when(orderUtilMock.paramsToObservable(submitParamsMock));
    }

    private OngoingStubbing<Single<SubmitParams>> stubGetSubmitParams() {
        return when(submitParamsFactoryMock.get(instrumentForTest, brokerBuyDataMock));
    }

    @Test
    public void submitFailsWhenSubmitParamsFail() {
        stubGetSubmitParams().thenReturn(Single.error(jfException));

        subscribe().assertError(jfException);
    }

    public class OnSubmitParamsPass {

        @Before
        public void setUp() {
            stubGetSubmitParams().thenReturn(Single.just(submitParamsMock));
        }

        @Test
        public void submitFailsWhenOrderUtilFails() {
            stubParamsToObservable().thenReturn(Observable.error(jfException));

            subscribe().assertError(jfException);
        }

        @Test
        public void submitSucceedsWhenOrderUtilSucceeds() {
            stubParamsToObservable().thenReturn(Observable.just(orderEventA, orderEventB));

            subscribe()
                .assertValue(orderMockB)
                .assertComplete();
        }
    }
}
