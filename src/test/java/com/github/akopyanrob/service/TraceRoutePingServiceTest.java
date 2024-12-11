package com.github.akopyanrob.service;

import com.github.akopyanrob.result.PingResult;
import com.github.akopyanrob.result.TraceRoutePingResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class TraceRoutePingServiceTest {

    private ProcessBuilder processBuilder;
    private Process mockProcess;
    private TraceRoutePingService traceRoutePingService;

    @BeforeEach
    void setUp() {
        processBuilder = Mockito.mock(ProcessBuilder.class);
        mockProcess = Mockito.mock(Process.class);
        traceRoutePingService = new TraceRoutePingService(processBuilder);
    }

    @Test
    public void testTraceRouteSuccess() throws Exception {
        String host = "example.com";
        String simulatedOutput = """
                Tracing route to example.com [172.217.5.110] over a maximum of 30 hops:
                  1    <1 ms    <1 ms    <1 ms  192.168.1.1
                  2    10 ms    10 ms    10 ms  172.217.5.110
                Trace complete.
        """;

        InputStream mockInputStream = new ByteArrayInputStream(simulatedOutput.getBytes());
        when(processBuilder.start()).thenReturn(mockProcess);
        when(mockProcess.getInputStream()).thenReturn(mockInputStream);

        PingResult result = traceRoutePingService.ping(host);

        assertTrue(result.isSuccessful());
        assertEquals(host, result.getHost());
        assertInstanceOf(TraceRoutePingResult.class, result);
        TraceRoutePingResult traceRoutePingResult = (TraceRoutePingResult) result;
        assertFalse(traceRoutePingResult.getResultLines().toString().contains("Request timed out."));
        assertFalse(traceRoutePingResult.getResultLines().toString().matches(".*\\* \\* \\*.*"));
    }

    @Test
    public void testTraceRouteFailure() throws Exception {
        String host = "example.com";
        String simulatedOutput = """
                Tracing route to example.com [172.217.5.110] over a maximum of 30 hops:
                  1    10 ms    10 ms    10 ms  192.168.1.1
                  2    * * *    Request timed out.
                  3    * * *    Request timed out.",
                Trace complete.
        """;

        InputStream mockInputStream = new ByteArrayInputStream(simulatedOutput.getBytes());
        when(processBuilder.start()).thenReturn(mockProcess);
        when(mockProcess.getInputStream()).thenReturn(mockInputStream);

        PingResult result = traceRoutePingService.ping(host);

        assertFalse(result.isSuccessful());
        assertEquals(host, result.getHost());
        assertInstanceOf(TraceRoutePingResult.class, result);
        TraceRoutePingResult traceRoutePingResult = (TraceRoutePingResult) result;
        assertTrue(traceRoutePingResult.getResultLines().toString().contains("Request timed out."));
        assertTrue(traceRoutePingResult.getResultLines().toString().matches(".*\\* \\* \\*.*"));
    }

    @Test
    public void testErrorDuringTraceRoute() throws Exception {
        String host = "example.com";

        when(processBuilder.command(anyString())).thenReturn(processBuilder);
        when(processBuilder.start()).thenThrow(new IOException("Test Exception"));

        PingResult result = traceRoutePingService.ping(host);

        assertFalse(result.isSuccessful());
        assertInstanceOf(TraceRoutePingResult.class, result);
        TraceRoutePingResult traceRoutePingResult = (TraceRoutePingResult) result;
        assertEquals("Error: Test Exception", traceRoutePingResult.getResultLines().get(0));
    }
}

