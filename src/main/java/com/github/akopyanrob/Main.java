package com.github.akopyanrob;

import com.github.akopyanrob.result.LastResultHolder;
import com.github.akopyanrob.report.ReportBuilder;
import com.github.akopyanrob.report.Reporter;
import com.github.akopyanrob.service.Config;
import com.github.akopyanrob.service.ICMPPingService;
import com.github.akopyanrob.scheduler.PingScheduler;
import com.github.akopyanrob.service.TCPPingService;
import com.github.akopyanrob.service.TraceRoutePingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final String APPLICATION_PROPERTIES = "application.properties";

    public static void main(String[] args) {

        Properties properties = loadProperties();

        LastResultHolder lastResultHolder = LastResultHolder.getInstance();

        ReportBuilder reportBuilder = new ReportBuilder(lastResultHolder);
        String reportDestinationUrl = getReportDestinationUrl(properties);
        Reporter reporter = new Reporter(reportBuilder, HttpClient.newBuilder().build(), reportDestinationUrl);

        // Start the pingScheduler
        int maxThreadPoolSize = getMaxThreadPoolSize(properties);
        int poolSize = Math.min(Runtime.getRuntime().availableProcessors() * 2, maxThreadPoolSize); // Max 16 threads as Ping calls are usually fast
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(poolSize);
        PingScheduler pingScheduler = new PingScheduler(scheduler, lastResultHolder, reporter);

        // Start TCP Ping Service
        Config tcpPingProperties = getTCPPingProperties(properties);
        HttpClient httpClient = HttpClient.newBuilder().build();
        TCPPingService tcpPingService = new TCPPingService(httpClient, tcpPingProperties);
        pingScheduler.schedulePing(tcpPingService, tcpPingProperties);

        // Start ICMP Ping Service
        ProcessBuilder icmpProcessBuilder = new ProcessBuilder();
        ICMPPingService icmpPingService = new ICMPPingService(icmpProcessBuilder);
        pingScheduler.schedulePing(icmpPingService, getICMPPingProperties(properties));

        // Start Trace Route Ping Service
        ProcessBuilder traceRouteProcessBuilder = new ProcessBuilder();
        TraceRoutePingService traceRoutePingService = new TraceRoutePingService(traceRouteProcessBuilder);
        pingScheduler.schedulePing(traceRoutePingService, getTraceRoutePingProperties(properties));
    }

    private static int getMaxThreadPoolSize(Properties properties) {
        String poolSizeStr = properties.getProperty("maxThreads", "16");
        try {
            return Integer.parseInt(poolSizeStr);
        } catch (NumberFormatException e) {
            logger.warn("Invalid max thread pool size in configuration: {}\nSetting max thread pool size as 16", poolSizeStr);
            return 16;
        }
    }

    private static String getReportDestinationUrl(Properties properties) {
        String reportDestination = properties.getProperty("report.url");
        if (reportDestination == null || reportDestination.isBlank()) {
            logger.error("'report.url' property is not specified in application.properties");
            System.exit(1);
        }
        return reportDestination;
    }

    private static String[] getHosts(Properties properties) {
        String hostsProperty = properties.getProperty("hosts");
        if (hostsProperty == null || hostsProperty.isEmpty()) {
            logger.error("'hosts' property is not specified in application.properties");
            System.exit(1);
        }

        return properties.getProperty("hosts").split(";");
    }

    private static Config getTCPPingProperties(Properties properties) {
        String[] hosts = getHosts(properties);
        int tcpPingDelay = Integer.parseInt(properties.getProperty("tcp.ping.delay.ms", "5000"));
        int timeout = Integer.parseInt(properties.getProperty("response.timeout.ms", "5000"));
        return new Config(hosts, tcpPingDelay, timeout);
    }

    private static Config getICMPPingProperties(Properties properties) {
        String[] hosts = getHosts(properties);
        int icmpPingDelay = Integer.parseInt(properties.getProperty("icmp.ping.delay.ms", "5000"));
        int timeout = Integer.parseInt(properties.getProperty("response.timeout.ms", "5000"));
        return new Config(hosts, icmpPingDelay, timeout);
    }

    private static Config getTraceRoutePingProperties(Properties properties) {
        String[] hosts = getHosts(properties);
        int tracePingDelay = Integer.parseInt(properties.getProperty("trace.ping.delay.ms", "5000"));
        int timeout = Integer.parseInt(properties.getProperty("response.timeout.ms", "5000"));
        return new Config(hosts, tracePingDelay, timeout);
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream inputStream = Main.class.getClassLoader().getResourceAsStream(APPLICATION_PROPERTIES)) {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration file: " + APPLICATION_PROPERTIES, e);
        }
        return properties;
    }
}
