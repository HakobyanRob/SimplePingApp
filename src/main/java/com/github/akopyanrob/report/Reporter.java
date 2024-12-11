package com.github.akopyanrob.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

public class Reporter {
    private static final Logger logger = LoggerFactory.getLogger(Reporter.class);

    private final ReportBuilder reportBuilder;
    private final HttpClient httpClient;
    private final String reportDestinationUrl;

    public Reporter(ReportBuilder reportBuilder, HttpClient httpClient, String reportDestinationUrl) {
        this.reportBuilder = Objects.requireNonNull(reportBuilder, "reportBuilder must not be null");
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient must not be null");
        this.reportDestinationUrl = Objects.requireNonNull(reportDestinationUrl, "reportDestinationUrl must not be null");
    }

    public void report(String host) {
        String report = reportBuilder.buildReport(host);
        System.out.println("REPORT : " + report);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://" + reportDestinationUrl))
                .header("Content-Type", "application/json; utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(report))
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                logger.debug("Report successfully sent: \n" + report);
            } else {
                logger.error("Failed to send report: \n" + report);
            }
        } catch (IOException | InterruptedException e) {
            logger.error("Exception when sending report: \n" + report, e);
        }
    }
}
