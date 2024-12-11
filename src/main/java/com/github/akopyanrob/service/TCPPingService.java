package com.github.akopyanrob.service;

import com.github.akopyanrob.result.PingResult;
import com.github.akopyanrob.result.TCPPingResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.Objects;

public class TCPPingService implements PingService {
    private static final Logger logger = LoggerFactory.getLogger(TCPPingService.class);

    private final HttpClient httpClient;
    private final Config config;

    public TCPPingService(HttpClient httpClient, Config config) {
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient must not be null");
        this.config = Objects.requireNonNull(config, "config must not be null");
    }

    @Override
    public PingResult ping(String host) {
        long startTime = System.currentTimeMillis();
        int responseCode;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + host))
                    .HEAD()
                    .timeout(Duration.ofMillis(config.timeout()))
                    .build();

            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            responseCode = response.statusCode();
            logger.debug("Successful TCP ping result for host " + host + ": " + response.statusCode());
        } catch (HttpTimeoutException | UnknownHostException | ConnectException | SocketTimeoutException ex) {
            logger.error("Error during tcp ping of host " + host + ": " + ex.getMessage());
            return new TCPPingResult(false, host, startTime, null, null);
        } catch (Exception e) {
            logger.error("Unexpected exception: " + e.getMessage());
            return null;
        }
        long endTime = System.currentTimeMillis();

        return new TCPPingResult(true, host, startTime, endTime - startTime, responseCode);
    }
}

