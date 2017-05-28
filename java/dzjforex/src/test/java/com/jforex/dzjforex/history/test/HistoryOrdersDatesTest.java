package com.jforex.dzjforex.history.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.time.LocalDateTime;
import java.time.Month;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.stubbing.OngoingStubbing;

import com.jforex.dzjforex.brokertime.ServerTimeProvider;
import com.jforex.dzjforex.history.HistoryOrdersDates;
import com.jforex.dzjforex.misc.TimeSpan;
import com.jforex.dzjforex.testutil.CommonUtilForTest;
import com.jforex.programming.misc.DateTimeUtil;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class HistoryOrdersDatesTest extends CommonUtilForTest {

    private HistoryOrdersDates historyOrdersDates;

    @Mock
    private ServerTimeProvider serverTimeProviderMock;
    private final int historyOrderInDays = 3;

    @Before
    public void setUp() {
        when(pluginConfigMock.historyOrderInDays()).thenReturn(historyOrderInDays);

        historyOrdersDates = new HistoryOrdersDates(serverTimeProviderMock, pluginConfigMock);
    }

    private TestObserver<TimeSpan> subscribe() {
        return historyOrdersDates
            .timeSpan()
            .test();
    }

    private OngoingStubbing<Single<Long>> stubServerTime() {
        return when(serverTimeProviderMock.get());
    }

    @Test
    public void timeSpanCallIsDeferred() {
        historyOrdersDates.timeSpan();

        verifyZeroInteractions(serverTimeProviderMock);
    }

    @Test
    public void whenServerTimeFailsErrorIsPropagated() {
        stubServerTime().thenReturn(Single.error(jfException));

        subscribe().assertError(jfException);
    }

    @Test
    public void returnedTimeSpanIsCorrect() {
        final LocalDateTime toDateTime = LocalDateTime.of(2017, Month.APRIL, 8, 12, 30);
        final LocalDateTime fromDateTime = toDateTime.minusDays(historyOrderInDays);
        final long to = DateTimeUtil.millisFromDateTime(toDateTime);
        final long from = DateTimeUtil.millisFromDateTime(fromDateTime);
        stubServerTime().thenReturn(Single.just(to));

        final TimeSpan timeSpan = (TimeSpan) subscribe()
            .getEvents()
            .get(0)
            .get(0);

        assertThat(timeSpan.from(), equalTo(from));
        assertThat(timeSpan.to(), equalTo(to));
    }
}
