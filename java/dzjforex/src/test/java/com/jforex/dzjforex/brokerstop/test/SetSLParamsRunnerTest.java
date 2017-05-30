package com.jforex.dzjforex.brokerstop.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.stubbing.OngoingStubbing;

import com.jforex.dzjforex.brokerstop.SetSLParamsFactory;
import com.jforex.dzjforex.brokerstop.SetSLParamsRunner;
import com.jforex.dzjforex.testutil.CommonUtilForTest;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.params.basic.SetSLParams;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class SetSLParamsRunnerTest extends CommonUtilForTest {

    private SetSLParamsRunner setSLParamsRunner;

    @Mock
    private SetSLParamsFactory orderSetSLParamsMock;
    @Mock
    private SetSLParams setSLParamsMock;

    @Before
    public void setUp() {
        setSLParamsRunner = new SetSLParamsRunner(orderUtilMock, orderSetSLParamsMock);
    }

    private OngoingStubbing<Observable<OrderEvent>> stubParamsToObservable() {
        return when(orderUtilMock.paramsToObservable(setSLParamsMock));
    }

    private OngoingStubbing<Single<SetSLParams>> stubGetSLParams() {
        return when(orderSetSLParamsMock.get(orderMockA, brokerStopDataMock));
    }

    private TestObserver<Void> subscribe() {
        return setSLParamsRunner
            .get(orderMockA, brokerStopDataMock)
            .test();
    }

    @Test
    public void getCallIsDeferred() {
        setSLParamsRunner.get(orderMockA, brokerStopDataMock);

        verifyZeroInteractions(orderUtilMock);
        verifyZeroInteractions(orderSetSLParamsMock);
    }

    @Test
    public void setSLFailsWhenSetSLParamsFail() {
        stubGetSLParams().thenReturn(Single.error(jfException));

        subscribe().assertError(jfException);
    }

    public class OnSetSLParamsPass {

        @Before
        public void setUp() {
            stubGetSLParams().thenReturn(Single.just(setSLParamsMock));
        }

        @Test
        public void setSLFailsWhenOrderUtilFails() {
            stubParamsToObservable().thenReturn(Observable.error(jfException));

            subscribe().assertError(jfException);
        }

        @Test
        public void setSLSucceedsWhenOrderUtilSucceeds() {
            stubParamsToObservable().thenReturn(Observable.just(orderEventA));

            subscribe().assertComplete();
        }
    }
}
