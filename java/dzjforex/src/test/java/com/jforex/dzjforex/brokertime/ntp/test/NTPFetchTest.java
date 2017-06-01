package com.jforex.dzjforex.brokertime.ntp.test;
//package com.jforex.dzjforex.brokertime.test;
//
//import static org.powermock.api.mockito.PowerMockito.mockStatic;
//
//import java.io.IOException;
//import java.net.InetAddress;
//
//import org.apache.commons.net.ntp.NTPUDPClient;
//import org.apache.commons.net.ntp.NtpV3Packet;
//import org.apache.commons.net.ntp.TimeInfo;
//import org.apache.commons.net.ntp.TimeStamp;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.Mock;
//import org.powermock.core.classloader.annotations.PowerMockIgnore;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;
//
//import com.jforex.dzjforex.brokertime.NTPFetch;
//import com.jforex.dzjforex.test.util.CommonUtilForTest;
//
//import io.reactivex.observers.TestObserver;
//
//@RunWith(PowerMockRunner.class)
//@PrepareForTest({ NTPUDPClient.class, InetAddress.class })
//@PowerMockIgnore("javax.management.*")
//public class NTPFetchTest extends CommonUtilForTest {
//
//    private NTPFetch ntpFetch;
//
//    @Mock
//    private InetAddress inetAddressMock;
//    @Mock
//    private TimeInfo timeInfoMock;
//    @Mock
//    private NtpV3Packet ntpV3PacketMock;
//    @Mock
//    private TimeStamp timeStampMock;
//
//    private NTPUDPClient ntpUTPClientMock;
//    private final String ntpServerURL = "someURL";
//    private final long ntp = 14L;
//
//    @Before
//    public void setUp() throws IOException {
//        when(pluginConfigMock.ntpServerURL()).thenReturn(ntpServerURL);
//
//        setUpMocks();
//
//        ntpFetch = new NTPFetch(ntpUTPClientMock, pluginConfigMock);
//    }
//
//    private void setUpMocks() throws IOException {
//        ntpUTPClientMock = mock(NTPUDPClient.class);
//        mockStatic(InetAddress.class);
//
//        when(InetAddress.getByName(ntpServerURL)).thenReturn(inetAddressMock);
//
//        when(ntpUTPClientMock.getTime(inetAddressMock)).thenReturn(timeInfoMock);
//        when(timeInfoMock.getMessage()).thenReturn(ntpV3PacketMock);
//        when(ntpV3PacketMock.getTransmitTimeStamp()).thenReturn(timeStampMock);
//        when(timeStampMock.getTime()).thenReturn(ntp);
//    }
//
//    private TestObserver<Long> subscribe() {
//        return ntpFetch
//            .get()
//            .test();
//    }
//
//    @Test
//    public void getCallIsDeferred() {
//        ntpFetch.get();
//
//        verifyZeroInteractions(ntpUTPClientMock);
//    }
//
//    @Test
//    public void ntpIsReturned() {
//        subscribe()
//            .assertValue(ntp)
//            .assertComplete();
//    }
//}
