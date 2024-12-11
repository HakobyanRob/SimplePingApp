package com.github.akopyanrob.scheduler;

import com.github.akopyanrob.report.Reporter;
import com.github.akopyanrob.result.ICMPPingResult;
import com.github.akopyanrob.result.LastResultHolder;
import com.github.akopyanrob.result.TCPPingResult;
import com.github.akopyanrob.result.TraceRoutePingResult;
import com.github.akopyanrob.service.Config;
import com.github.akopyanrob.service.ICMPPingService;
import com.github.akopyanrob.service.PingService;
import com.github.akopyanrob.service.TCPPingService;
import com.github.akopyanrob.service.TraceRoutePingService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PingSchedulerTest {
    private ScheduledExecutorService scheduler;
    private LastResultHolder lastResultHolder;
    private Reporter reporter;
    private Config config;
    private static final String HOST = "host";

    @BeforeEach
    void setUp() {
        scheduler = Executors.newScheduledThreadPool(32);

        lastResultHolder = mock(LastResultHolder.class);
        reporter = mock(Reporter.class);

        config = new Config(new String[]{HOST}, 300, 400);
    }

    @Test
    void constructor_nullScheduler_throwsException() {
        assertThrows(NullPointerException.class, () -> new PingScheduler(null, lastResultHolder, reporter));
    }

    @Test
    void constructor_nullLastResultHolder_throwsException() {
        assertThrows(NullPointerException.class, () -> new PingScheduler(scheduler, null, reporter));
    }

    @Test
    void constructor_nullReporter_throwsException() {
        assertThrows(NullPointerException.class, () -> new PingScheduler(scheduler, lastResultHolder, null));
    }

    @Test
    void icmpPingSuccess() {
        ScheduledExecutorService mockScheduler = mock(ScheduledExecutorService.class);

        var pingScheduler = new PingScheduler(mockScheduler, lastResultHolder, reporter);

        var icmpPingService = mock(ICMPPingService.class);
        List<String> resultLines = new ArrayList<>();
        resultLines.add("PING");
        var icmpPingResult = new ICMPPingResult(true, HOST, System.currentTimeMillis(), resultLines);

        when(icmpPingService.ping(HOST)).thenReturn(icmpPingResult);

        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(mockScheduler).scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(), any());

        pingScheduler.schedulePing(icmpPingService, config);

        verify(icmpPingService, times(1)).ping(HOST);
        verify(lastResultHolder, times(1)).addHostLastICMPPingResult(HOST, icmpPingResult);
        verify(reporter, times(0)).report(HOST);
    }

    @Test
    void icmpPingFailure() {
        ScheduledExecutorService mockScheduler = mock(ScheduledExecutorService.class);

        var pingScheduler = new PingScheduler(mockScheduler, lastResultHolder, reporter);

        var icmpPingService = mock(ICMPPingService.class);
        List<String> resultLines = new ArrayList<>();
        resultLines.add("PING");
        var icmpPingResult = new ICMPPingResult(false, HOST, System.currentTimeMillis(), resultLines);

        when(icmpPingService.ping(HOST)).thenReturn(icmpPingResult);

        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(mockScheduler).scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(), any());

        pingScheduler.schedulePing(icmpPingService, config);

        verify(icmpPingService, times(1)).ping(HOST);
        verify(lastResultHolder, times(1)).addHostLastICMPPingResult(HOST, icmpPingResult);
        verify(reporter, times(1)).report(HOST);
    }

    @Test
    void tcpPingSuccess() {
        ScheduledExecutorService mockScheduler = mock(ScheduledExecutorService.class);

        var pingScheduler = new PingScheduler(mockScheduler, lastResultHolder, reporter);

        var tcpPingService = mock(TCPPingService.class);
        List<String> resultLines = new ArrayList<>();
        resultLines.add("PING");

        long currentTimeMillis = System.currentTimeMillis();
        var tcpPingResult = new TCPPingResult(true, HOST, currentTimeMillis, currentTimeMillis + 100000L, 200);

        when(tcpPingService.ping(HOST)).thenReturn(tcpPingResult);

        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(mockScheduler).scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(), any());

        pingScheduler.schedulePing(tcpPingService, config);

        verify(tcpPingService, times(1)).ping(HOST);
        verify(lastResultHolder, times(1)).addHostLastTCPPingResult(HOST, tcpPingResult);
        verify(reporter, times(0)).report(HOST);
    }

    @Test
    void tcpPingFailure() {
        ScheduledExecutorService mockScheduler = mock(ScheduledExecutorService.class);

        var pingScheduler = new PingScheduler(mockScheduler, lastResultHolder, reporter);

        var tcpPingService = mock(TCPPingService.class);
        List<String> resultLines = new ArrayList<>();
        resultLines.add("PING");

        long currentTimeMillis = System.currentTimeMillis();
        var tcpPingResult = new TCPPingResult(false, HOST, currentTimeMillis, currentTimeMillis + 100000L, 500);

        when(tcpPingService.ping(HOST)).thenReturn(tcpPingResult);

        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(mockScheduler).scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(), any());

        pingScheduler.schedulePing(tcpPingService, config);

        verify(tcpPingService, times(1)).ping(HOST);
        verify(lastResultHolder, times(1)).addHostLastTCPPingResult(HOST, tcpPingResult);
        verify(reporter, times(1)).report(HOST);
    }

    @Test
    void tracePingSuccess() {
        var mockScheduler = mock(ScheduledExecutorService.class);

        var pingScheduler = new PingScheduler(mockScheduler, lastResultHolder, reporter);

        var traceRoutePingService = mock(TraceRoutePingService.class);
        List<String> resultLines = new ArrayList<>();
        resultLines.add("PING");
        var traceRoutePingResult = new TraceRoutePingResult(true, HOST, System.currentTimeMillis(), resultLines);

        when(traceRoutePingService.ping(HOST)).thenReturn(traceRoutePingResult);

        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(mockScheduler).scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(), any());

        pingScheduler.schedulePing(traceRoutePingService, config);

        verify(traceRoutePingService, times(1)).ping(HOST);
        verify(lastResultHolder, times(1)).addHostLastTracePingResult(HOST, traceRoutePingResult);
        verify(reporter, times(0)).report(HOST);
    }

    @Test
    void tracePingFailure() {
        var mockScheduler = mock(ScheduledExecutorService.class);

        PingScheduler pingScheduler = new PingScheduler(mockScheduler, lastResultHolder, reporter);

        var traceRoutePingService = mock(TraceRoutePingService.class);
        List<String> resultLines = new ArrayList<>();
        resultLines.add("PING");
        var traceRoutePingResult = new TraceRoutePingResult(false, HOST, System.currentTimeMillis(), resultLines);

        when(traceRoutePingService.ping(HOST)).thenReturn(traceRoutePingResult);

        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(mockScheduler).scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(), any());

        pingScheduler.schedulePing(traceRoutePingService, config);

        verify(traceRoutePingService, times(1)).ping(HOST);
        verify(lastResultHolder, times(1)).addHostLastTracePingResult(HOST, traceRoutePingResult);
        verify(reporter, times(0)).report(HOST);
    }

    @AfterEach
    void tearDown() {
        scheduler.shutdownNow();
    }
}
