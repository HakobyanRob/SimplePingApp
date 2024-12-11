package com.github.akopyanrob.scheduler;

import com.github.akopyanrob.service.Config;
import com.github.akopyanrob.result.ICMPPingResult;
import com.github.akopyanrob.result.PingResult;
import com.github.akopyanrob.result.TCPPingResult;
import com.github.akopyanrob.result.TraceRoutePingResult;
import com.github.akopyanrob.result.LastResultHolder;
import com.github.akopyanrob.report.Reporter;
import com.github.akopyanrob.service.PingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PingScheduler {
    private static final Logger logger = LoggerFactory.getLogger(PingScheduler.class);

    private final ScheduledExecutorService scheduler;
    private final LastResultHolder lastResultHolder;
    private final Reporter reporter;

    public PingScheduler(ScheduledExecutorService scheduler, LastResultHolder lastResultHolder, Reporter reporter) {
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler must not be null");
        this.lastResultHolder = Objects.requireNonNull(lastResultHolder, "lastResultHolder must not be null");
        this.reporter = Objects.requireNonNull(reporter, "reporter must not be null");
    }

    //we could use futures
    public void schedulePing(PingService pingService, Config config) {
        for (String host : config.hosts()) {
            scheduler.scheduleWithFixedDelay(() -> {
                try {
                    PingResult result = pingService.ping(host);
                    logger.debug("Ping result: {}", result);

                    switch (result) {
                        case ICMPPingResult icmpPingResult -> {
                            lastResultHolder.addHostLastICMPPingResult(host, result);
                            if (!icmpPingResult.isSuccessful()) {
                                reporter.report(host);
                            }
                        }
                        case TCPPingResult tcpPingResult -> {
                            lastResultHolder.addHostLastTCPPingResult(host, result);
                            if (!tcpPingResult.isSuccessful()) {
                                reporter.report(host);
                            }
                        }
                        case TraceRoutePingResult traceRoutePingResult -> {
                            lastResultHolder.addHostLastTracePingResult(host, result);
                        }
                        default -> throw new RuntimeException("Unexpected PingResult: " + result);
                    }
                } catch (Exception e) {
                    logger.error("Error occurred during ping for host {}: {}", host, e.getMessage(), e);
                }
            }, 0, config.pingDelay(), TimeUnit.MILLISECONDS);
        }
    }
}
