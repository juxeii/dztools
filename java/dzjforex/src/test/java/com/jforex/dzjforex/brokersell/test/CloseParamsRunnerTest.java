package com.jforex.dzjforex.brokersell.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.stubbing.OngoingStubbing;

import com.jforex.dzjforex.brokersell.CloseParamsFactory;
import com.jforex.dzjforex.brokersell.CloseParamsRunner;
import com.jforex.dzjforex.testutil.CommonUtilForTest;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.params.basic.CloseParams;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class CloseParamsRunnerTest extends CommonUtilForTest {

    private CloseParamsRunner closeParamsRunner;

    @Mock
    private CloseParamsFactory orderCloseParamsMock;
    @Mock
    private CloseParams closeParamsMock;

    @Before
    public void setUp() {
        closeParamsRunner = new CloseParamsRunner(orderUtilMock, orderCloseParamsMock);
    }

    private OngoingStubbing<Observable<OrderEvent>> stubParamsToObservable() {
        return when(orderUtilMock.paramsToObservable(closeParamsMock));
    }

    private OngoingStubbing<Single<CloseParams>> stubGetCloseParams() {
        return when(orderCloseParamsMock.get(orderMockA, brokerSellDataMock));
    }

    private TestObserver<Void> subscribe() {
        return closeParamsRunner
            .get(orderMockA, brokerSellDataMock)
            .test();
    }

    @Test
    public void closeFailsWhenCloseParamsFail() {
        stubGetCloseParams().thenReturn(Single.error(jfException));

        subscribe().assertError(jfException);
    }

    public class OnCloseParamsPass {

        @Before
        public void setUp() {
            stubGetCloseParams().thenReturn(Single.just(closeParamsMock));
        }

        @Test
        public void closeFailsWhenOrderUtilFails() {
            stubParamsToObservable().thenReturn(Observable.error(jfException));

            subscribe().assertError(jfException);
        }

        @Test
        public void closeSucceedsWhenOrderUtilSucceeds() {
            stubParamsToObservable().thenReturn(Observable.just(orderEventA));

            subscribe().assertComplete();
        }
    }
}
