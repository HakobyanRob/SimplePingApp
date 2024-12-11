package com.github.akopyanrob.service;

import com.github.akopyanrob.result.PingResult;
import com.github.akopyanrob.result.TCPPingResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TCPPingServiceTest {

    private HttpClient httpClient;
    private Config config;
    private TCPPingService tcpPingService;

    @BeforeEach
    void setUp() {
        httpClient = mock(HttpClient.class);
        config = new Config(new String[]{"Host"}, 300, 400);
        tcpPingService = new TCPPingService(httpClient, config);
    }

    @Test
    void ping_successfulResponse_returnsPingResult() throws Exception {
        String host = "example.com";
        HttpResponse<Void> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(response);

        PingResult result = tcpPingService.ping(host);

        assertNotNull(result);
        assertTrue(result.isSuccessful());
        assertEquals(host, result.getHost());
        assertTrue(((TCPPingResult) result).getResponseTimeMillis() > 0);
        assertEquals(200, ((TCPPingResult) result).getResponseCode());
    }

    @Test
    void ping_httpTimeoutException_returnsFailedPingResult() throws Exception {
        String host = "example.com";
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new HttpTimeoutException("Timeout"));

        PingResult result = tcpPingService.ping(host);

        assertNotNull(result);
        assertFalse(result.isSuccessful());
        assertEquals(host, result.getHost());
        assertNull(((TCPPingResult) result).getResponseTimeMillis());
        assertNull(((TCPPingResult) result).getResponseCode());
    }

    @Test
    void ping_unknownHostException_returnsFailedPingResult() throws Exception {
        String host = "unknown-host";
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new UnknownHostException("Unknown host"));

        PingResult result = tcpPingService.ping(host);

        assertNotNull(result);
        assertFalse(result.isSuccessful());
        assertEquals(host, result.getHost());
        assertNull(((TCPPingResult) result).getResponseTimeMillis());
        assertNull(((TCPPingResult) result).getResponseCode());
    }

    @Test
    void ping_connectException_returnsFailedPingResult() throws Exception {
        String host = "example.com";
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new ConnectException("Connection failed"));

        PingResult result = tcpPingService.ping(host);

        assertNotNull(result);
        assertFalse(result.isSuccessful());
        assertEquals(host, result.getHost());
        assertNull(((TCPPingResult) result).getResponseTimeMillis());
        assertNull(((TCPPingResult) result).getResponseCode());
    }

    @Test
    void ping_generalException_returnsNull() throws Exception {
        String host = "example.com";
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new RuntimeException("General error"));

        PingResult result = tcpPingService.ping(host);

        assertNull(result);
    }
}