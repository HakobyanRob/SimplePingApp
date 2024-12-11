package com.github.akopyanrob.service;

import com.github.akopyanrob.result.PingResult;
import com.github.akopyanrob.result.TraceRoutePingResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class TraceRoutePingService extends ProcessPingService {
    private static final Logger logger = LoggerFactory.getLogger(TraceRoutePingService.class);

    public TraceRoutePingService(ProcessBuilder processBuilder) {
        super(processBuilder);
    }

    @Override
    protected String getPingCommand(String host) {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return String.format("tracert %s", host);
        } else {
            return String.format("traceroute -I %s", host);
        }
    }

    @Override
    protected boolean parsePingOutput(List<String> outputLines) {
        for (String line : outputLines) {
            if (line.contains("Request timed out.") || line.matches(".*\\* \\* \\*.*")) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected PingResult createPingResult(boolean success, String host, long startTime, List<String> outputLines) {
        logger.debug("Trace ping result for host " + host + ":\n " + outputLines);
        return new TraceRoutePingResult(success, host, startTime, outputLines);
    }

    @Override
    protected PingResult handleError(String host, long startTime, String errorMessage) {
        List<String> errorLines = new ArrayList<>();
        errorLines.add("Error: " + errorMessage);
        return new TraceRoutePingResult(false, host, startTime, errorLines);
    }
}

