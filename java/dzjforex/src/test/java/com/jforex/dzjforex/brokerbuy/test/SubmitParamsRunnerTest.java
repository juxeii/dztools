//package com.jforex.dzjforex.brokerbuy.test;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.Mock;
//
//import com.jforex.dzjforex.brokerbuy.OrderSubmitParams;
//import com.jforex.dzjforex.brokerbuy.SubmitParamsRunner;
//import com.jforex.dzjforex.test.util.CommonUtilForTest;
//import com.jforex.programming.order.task.params.basic.SubmitParams;
//
//import de.bechte.junit.runners.context.HierarchicalContextRunner;
//import io.reactivex.Observable;
//import io.reactivex.Single;
//import io.reactivex.observers.TestObserver;
//
//@RunWith(HierarchicalContextRunner.class)
//public class SubmitParamsRunnerTest extends CommonUtilForTest {
//
//    private SubmitParamsRunner submitParamsRunner;
//
//    @Mock
//    private OrderSubmitParams orderSubmitParamsMock;
//    @Mock
//    private SubmitParams submitParamsMock;
//
//    @Before
//    public void setUp() {
//        submitParamsRunner = new SubmitParamsRunner(orderUtilMock, orderSubmitParamsMock);
//    }
//
//    private void makeOrderUtilPass() {
//        when(orderUtilMock.paramsToObservable(submitParamsMock))
//            .thenReturn(Observable.just(orderEvent));
//    }
//
//    private void makeOrderUtilFail() {
//        when(orderUtilMock.paramsToObservable(submitParamsMock))
//            .thenReturn(Observable.error(jfException));
//    }
//
//    private void makeSubmitParamsPass() {
//        when(orderSubmitParamsMock.get(orderMock, brokerStopData))
//            .thenReturn(Single.just(submitParamsMock));
//    }
//
//    private void makeSetSLParamsFail() {
//        when(orderSetSLParamsMock.get(orderMock, brokerStopData))
//            .thenReturn(Single.error(jfException));
//    }
//
//    private TestObserver<Void> subscribe() {
//        return submitParamsRunner
//            .get(orderMock, brokerStopData)
//            .test();
//    }
//
//    @Test
//    public void setSLFailsWhenSetSLParamsFail() {
//        makeSetSLParamsFail();
//
//        subscribe().assertError(jfException);
//    }
//
//    public class OnSetSLParamsPass {
//
//        @Before
//        public void setUp() {
//            makeSetSLParamsPass();
//        }
//
//        @Test
//        public void setSLFailsWhenOrderUtilFails() {
//            makeOrderUtilFail();
//
//            subscribe().assertError(jfException);
//        }
//
//        @Test
//        public void setSLSucceedsWhenOrderUtilSucceeds() {
//            makeOrderUtilPass();
//
//            subscribe().assertComplete();
//        }
//    }
//}
