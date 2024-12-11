package com.github.akopyanrob.service;

import com.github.akopyanrob.result.ICMPPingResult;
import com.github.akopyanrob.result.PingResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * If portability is crucial and having a dependency is okay: we can use icmp4j.
 * If we need a quick-and-dirty solution and can afford platform dependency: we can use the ping command.
 * If we only need basic reachability checks (actually uses TCP/UDP): use InetAddress.isReachable().
 */
public class ICMPPingService extends ProcessPingService {
    private static final Logger logger = LoggerFactory.getLogger(ICMPPingService.class);

    private static final int PACKET_COUNT = 5;

    public ICMPPingService(ProcessBuilder processBuilder) {
        super(processBuilder);
    }

    @Override
    protected String getPingCommand(String host) {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return String.format("ping -n %d %s", PACKET_COUNT, host);
        } else {
            return String.format("ping -c %d %s", PACKET_COUNT, host);
        }
    }

    @Override
    protected boolean parsePingOutput(List<String> outputLines) {
        for (String line : outputLines) {
            if (line.contains("0% packet loss") || line.contains("Lost = 0")) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected PingResult createPingResult(boolean success, String host, long startTime, List<String> outputLines) {
        logger.debug("ICMP ping result for host " + host + ":\n " + outputLines);
        return new ICMPPingResult(success, host, startTime, outputLines);
    }

    @Override
    protected PingResult handleError(String host, long startTime, String errorMessage) {
        List<String> errorLines = new ArrayList<>();
        errorLines.add("Error: " + errorMessage);
        return new ICMPPingResult(false, host, startTime, errorLines);
    }
}

