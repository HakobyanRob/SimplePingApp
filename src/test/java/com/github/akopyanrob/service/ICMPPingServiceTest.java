package com.github.akopyanrob.service;

import com.github.akopyanrob.result.ICMPPingResult;
import com.github.akopyanrob.result.PingResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class ICMPPingServiceTest {

    private ProcessBuilder processBuilder;
    private Process mockProcess;
    private ICMPPingService icmpPingService;

    @BeforeEach
    void setUp() {
        processBuilder = Mockito.mock(ProcessBuilder.class);
        mockProcess = Mockito.mock(Process.class);
        icmpPingService = new ICMPPingService(processBuilder);
    }

    @Test
    void ping_successfulLinuxPing_returnsPingResult() throws Exception {
        String host = "example.com";
        String simulatedOutput = """
                Pinging example.com [0.0.0.0] with 32 bytes of data:
                Reply from 0.0.0.0: bytes=32 time=17ms TTL=59
                Reply from 0.0.0.0: bytes=32 time=12ms TTL=59
                Reply from 0.0.0.0: bytes=32 time=11ms TTL=59
                Reply from 0.0.0.0: bytes=32 time=12ms TTL=59
                Reply from 0.0.0.0: bytes=32 time=13ms TTL=59
                Ping statistics for 0.0.0.0:
                Packets: Sent = 5, Received = 5, Lost = 0 (0% loss),
                Approximate round trip times in milli-seconds:
                Minimum = 12ms, Maximum = 17ms, Average = 13ms
                """;

        InputStream mockInputStream = new ByteArrayInputStream(simulatedOutput.getBytes());
        when(processBuilder.start()).thenReturn(mockProcess);
        when(mockProcess.getInputStream()).thenReturn(mockInputStream);

        PingResult result = icmpPingService.ping(host);

        assertNotNull(result);
        assertTrue(result.isSuccessful());
        assertEquals(host, result.getHost());
        assertInstanceOf(ICMPPingResult.class, result);
        ICMPPingResult icmpResult = (ICMPPingResult) result;
        assertTrue(icmpResult.getResultLines().toString().contains("0% loss"));
    }

    @Test
    void ping_failedPing_returnsFailedPingResult() throws Exception {
        // Arrange
        String host = "example.com";
        String simulatedOutput = """
                Pinging example.com [0.0.0.0] with 32 bytes of data:
                Reply from 0.0.0.0: bytes=32 time=17ms TTL=59
                Reply from 0.0.0.0: bytes=32 time=12ms TTL=59
                Reply from 0.0.0.0: bytes=32 time=11ms TTL=59
                Reply from 0.0.0.0: bytes=32 time=12ms TTL=59
                Reply from 0.0.0.0: bytes=32 time=13ms TTL=59
                Ping statistics for 0.0.0.0:
                Packets: Sent = 5, Received = 1, Lost = 4 (80% loss),
                Approximate round trip times in milli-seconds:
                Minimum = 12ms, Maximum = 17ms, Average = 13ms
                """;

        InputStream mockInputStream = new ByteArrayInputStream(simulatedOutput.getBytes());
        when(processBuilder.start()).thenReturn(mockProcess);
        when(mockProcess.getInputStream()).thenReturn(mockInputStream);

        PingResult result = icmpPingService.ping(host);

        assertNotNull(result);
        assertFalse(result.isSuccessful());
        assertEquals(host, result.getHost());
        assertInstanceOf(ICMPPingResult.class, result);
        ICMPPingResult icmpResult = (ICMPPingResult) result;
        assertTrue(icmpResult.getResultLines().toString().contains("80% loss"));
    }

    @Test
    public void testErrorDuringPing() throws Exception {
        String host = "1.1.1.1";

        when(processBuilder.command(anyString())).thenReturn(processBuilder);
        when(processBuilder.start()).thenThrow(new IOException("Test Exception"));

        PingResult result = icmpPingService.ping(host);

        assertFalse(result.isSuccessful());
        assertInstanceOf(ICMPPingResult.class, result);
        ICMPPingResult icmpResult = (ICMPPingResult) result;
        assertEquals("Error: Test Exception", icmpResult.getResultLines().get(0));
    }
}
