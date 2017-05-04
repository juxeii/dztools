//package com.jforex.dzjforex.order.test;
//
//import static org.hamcrest.Matchers.equalTo;
//import static org.junit.Assert.assertThat;
//import static org.junit.Assert.assertTrue;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.Mock;
//
//import com.dukascopy.api.IOrder;
//import com.jforex.dzjforex.history.HistoryOrders;
//import com.jforex.dzjforex.order.OrderLabelUtil;
//import com.jforex.dzjforex.order.OrderRepository;
//import com.jforex.dzjforex.order.OpenOrders;
//import com.jforex.dzjforex.test.util.CommonOrderForTest;
//
//import de.bechte.junit.runners.context.HierarchicalContextRunner;
//
//@RunWith(HierarchicalContextRunner.class)
//public class OrderRepositoryTest extends CommonOrderForTest {
//
//    private OrderRepository orderRepository;
//
//    @Mock
//    private OpenOrders runningOrdersMock;
//    @Mock
//    private HistoryOrders historyOrdersMock;
//    @Mock
//    private OrderLabelUtil labelUtilMock;
//    @Mock
//    private IOrder storedOrderMock;
//    @Mock
//    private IOrder runningOrderMock;
//    @Mock
//    private IOrder historyOrderMock;
//    private final List<IOrder> runningOrders = new ArrayList<>();
//    private final List<IOrder> historyOrders = new ArrayList<>();
//
//    @Before
//    public void setUp() {
//        when(runningOrdersMock.get()).thenReturn(runningOrders);
//
//        when(historyOrdersMock.get()).thenReturn(historyOrders);
//
//        orderRepository = new OrderRepository(runningOrdersMock,
//                                              historyOrdersMock,
//                                              labelUtilMock);
//    }
//
//    @Test
//    public void afterCreationNoOrderIdIsPresent() {
//        assertTrue(orderRepository.orderByID(1) == null);
//
//        verify(runningOrdersMock).get();
//        verify(historyOrdersMock).get();
//    }
//
//    public class WithOneStoredOrder {
//
//        private final int storedOrderId = 1;
//        private final int runningOrderId = 2;
//        private final int historyOrderId = 3;
//
//        @Before
//        public void setUp() {
//            orderRepository.storeOrder(storedOrderId, storedOrderMock);
//        }
//
//        @Test
//        public void storedOrderIdIsFound() {
//            assertThat(orderRepository.orderByID(storedOrderId), equalTo(storedOrderMock));
//        }
//
//        @Test
//        public void noSeekingInRunningOrders() {
//            verifyZeroInteractions(runningOrdersMock);
//        }
//
//        @Test
//        public void noSeekingInHistoryOrders() {
//            verifyZeroInteractions(historyOrdersMock);
//        }
//
//        @Test
//        public void runningOrderIdNotFound() {
//            assertTrue(orderRepository.orderByID(runningOrderId) == null);
//        }
//
//        public class WithRunningOrderPresent {
//
//            private final String runningOrderLabel = "Zorro" + runningOrderId;
//            private IOrder returnedOrder;
//
//            @Before
//            public void setUp() {
//                when(labelUtilMock.hasZorroPrefix(runningOrderMock)).thenReturn(true);
//                when(labelUtilMock.idFromLabel(runningOrderLabel)).thenReturn(runningOrderId);
//
//                when(runningOrderMock.getLabel()).thenReturn(runningOrderLabel);
//
//                when(runningOrdersMock.get()).thenReturn(runningOrders);
//
//                runningOrders.add(runningOrderMock);
//
//                returnedOrder = orderRepository.orderByID(runningOrderId);
//            }
//
//            @Test
//            public void runningOrderIdIsFound() {
//                assertThat(returnedOrder, equalTo(runningOrderMock));
//            }
//
//            @Test
//            public void runningOrdersMockIsCalled() {
//                verify(runningOrdersMock).get();
//            }
//
//            @Test
//            public void noSeekingInHistoryOrders() {
//                verifyZeroInteractions(historyOrdersMock);
//            }
//
//            @Test
//            public void consecutiveSeekingOrderIdOnlySearchesOneTimeInRunningOrders() {
//                orderRepository.orderByID(runningOrderId);
//
//                verify(runningOrdersMock).get();
//                verifyZeroInteractions(historyOrdersMock);
//            }
//
//            public class WithHistoryOrderPresent {
//
//                private final String historyOrderLabel = "Zorro" + historyOrderId;
//                private IOrder returnedOrder;
//
//                @Before
//                public void setUp() {
//                    when(labelUtilMock.hasZorroPrefix(historyOrderMock)).thenReturn(true);
//                    when(labelUtilMock.idFromLabel(historyOrderLabel)).thenReturn(historyOrderId);
//
//                    when(historyOrderMock.getLabel()).thenReturn(historyOrderLabel);
//
//                    when(historyOrdersMock.get()).thenReturn(historyOrders);
//
//                    historyOrders.add(historyOrderMock);
//
//                    returnedOrder = orderRepository.orderByID(historyOrderId);
//                }
//
//                @Test
//                public void historyOrderIdIsFound() {
//                    assertThat(returnedOrder, equalTo(historyOrderMock));
//                }
//
//                @Test
//                public void historyOrdersMockIsCalled() {
//                    verify(historyOrdersMock).get();
//                }
//
//                @Test
//                public void consecutiveSeekingOrderIdOnlySearchesOneTimeInHistoryOrders() {
//                    orderRepository.orderByID(historyOrderId);
//
//                    verify(historyOrdersMock).get();
//                }
//            }
//        }
//    }
//}
