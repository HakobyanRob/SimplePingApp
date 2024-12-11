package com.github.akopyanrob.report;

import com.github.akopyanrob.result.LastResultHolder;
import com.github.akopyanrob.result.PingResult;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Objects;

public class ReportBuilder {
    private final LastResultHolder lastResultHolder;

    public ReportBuilder(LastResultHolder lastResultHolder) {
        this.lastResultHolder = Objects.requireNonNull(lastResultHolder, "lastResultHolder must not be null");
    }

    public String buildReport(String host) {
        LinkedHashMap<String, String> reportMap = new LinkedHashMap<>();

        reportMap.put("host", host);

        String icmpPingResult = getPingResultOrDefault(lastResultHolder.getHostLastICMPPingResult().get(host));
        reportMap.put("icmp_ping", icmpPingResult);

        String tcpPingResult = getPingResultOrDefault(lastResultHolder.getHostLastTCPPingResult().get(host));
        reportMap.put("tcp_ping", tcpPingResult);

        String traceRoutePingResult = getPingResultOrDefault(lastResultHolder.getHostLastTracePingResult().get(host));
        reportMap.put("trace", traceRoutePingResult);

        JSONObject jsonObject = new JSONObject(reportMap);
        return jsonObject.toString(4);
    }

    private String getPingResultOrDefault(PingResult pingResult) {
        return pingResult != null ? pingResult.toString() : "N/A";
    }
}
