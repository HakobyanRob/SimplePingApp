package com.github.akopyanrob.report;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ReporterTest {

    private ReportBuilder reportBuilder;
    private HttpClient httpClient;
    private HttpResponse<String> httpResponse;
    private Reporter reporter;

    private static final String REPORT_DESTINATION_URL = "example.com/report";

    @BeforeEach
    void setUp() {
        reportBuilder = mock(ReportBuilder.class);
        httpClient = mock(HttpClient.class);
        httpResponse = mock(HttpResponse.class);
        reporter = new Reporter(reportBuilder, httpClient, REPORT_DESTINATION_URL);
    }

    @Test
    void constructor_nullReportBuilder_throwsException() {
        assertThrows(NullPointerException.class, () -> new Reporter(null, httpClient, REPORT_DESTINATION_URL));
    }

    @Test
    void constructor_nullHttpClient_throwsException() {
        assertThrows(NullPointerException.class, () -> new Reporter(reportBuilder, null, REPORT_DESTINATION_URL));
    }

    @Test
    void constructor_nullReportDestinationUrl_throwsException() {
        assertThrows(NullPointerException.class, () -> new Reporter(reportBuilder, httpClient, null));
    }

    @Test
    void report_validHost_sendsReportSuccessfully() throws IOException, InterruptedException {
        String host = "host";
        String report = "{\"host\":\"host\"}";

        when(reportBuilder.buildReport(host)).thenReturn(report);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(200);

        reporter.report(host);

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient).send(requestCaptor.capture(), any(HttpResponse.BodyHandler.class));

        HttpRequest request = requestCaptor.getValue();
        assertEquals(URI.create("http://" + REPORT_DESTINATION_URL), request.uri());
        assertEquals("application/json; utf-8", request.headers().firstValue("Content-Type").orElse(null));
        verify(httpResponse, times(1)).statusCode();
    }

    @Test
    void report_invalidHost_failsToSendReport() throws IOException, InterruptedException {
        String host = "invalidHost";
        String report = "{\"host\":\"invalidHost\"}";

        when(reportBuilder.buildReport(host)).thenReturn(report);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(500);

        reporter.report(host);

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient).send(requestCaptor.capture(), any(HttpResponse.BodyHandler.class));

        HttpRequest request = requestCaptor.getValue();
        assertEquals(URI.create("http://" + REPORT_DESTINATION_URL), request.uri());
        assertEquals("application/json; utf-8", request.headers().firstValue("Content-Type").orElse(null));
        verify(httpResponse, times(1)).statusCode();
    }

    @Test
    void report_httpClientThrowsException_throwsRuntimeException() throws IOException, InterruptedException {
        String host = "host";
        String report = "{\"host\":\"host\"}";

        when(reportBuilder.buildReport(host)).thenReturn(report);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenThrow(IOException.class);

        assertDoesNotThrow(() -> reporter.report(host));

        verify(httpClient, times(1)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }
}

