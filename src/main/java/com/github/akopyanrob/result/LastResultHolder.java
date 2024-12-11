package com.github.akopyanrob.result;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LastResultHolder {

    private final ConcurrentHashMap<String, PingResult> hostLastICMPPingResult = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, PingResult> hostLastTCPPingResult = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, PingResult> hostLastTracePingResult = new ConcurrentHashMap<>();

    //Bill Pugh Singleton Design
    private LastResultHolder() {
    }

    private static class Holder {
        private static final LastResultHolder INSTANCE = new LastResultHolder();
    }

    public static LastResultHolder getInstance() {
        return Holder.INSTANCE;
    }

    public Map<String, PingResult> getHostLastICMPPingResult() {
        return Collections.unmodifiableMap(hostLastICMPPingResult);
    }

    public Map<String, PingResult> getHostLastTCPPingResult() {
        return Collections.unmodifiableMap(hostLastTCPPingResult);
    }

    public Map<String, PingResult> getHostLastTracePingResult() {
        return Collections.unmodifiableMap(hostLastTracePingResult);
    }

    public void addHostLastICMPPingResult(String host, PingResult lastICMPPingResult) {
        validateInput(host, lastICMPPingResult);
        this.hostLastICMPPingResult.put(host, lastICMPPingResult);
    }

    public void addHostLastTCPPingResult(String host, PingResult lastTCPPingResult) {
        validateInput(host, lastTCPPingResult);
        this.hostLastTCPPingResult.put(host, lastTCPPingResult);
    }

    public void addHostLastTracePingResult(String host, PingResult lastTracePingResult) {
        validateInput(host, lastTracePingResult);
        this.hostLastTracePingResult.put(host, lastTracePingResult);
    }

    private static void validateInput(String host, PingResult lastTracePingResult) {
        if (host == null || lastTracePingResult == null) {
            throw new IllegalArgumentException("Host and result must not be null");
        }
    }
}
