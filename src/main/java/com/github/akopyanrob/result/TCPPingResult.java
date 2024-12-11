package com.github.akopyanrob.result;

import org.json.JSONObject;

import java.util.Objects;

public final class TCPPingResult extends PingResult {
    private final Long responseTimeMillis;
    private final Integer responseCode;

    public TCPPingResult(boolean isSuccessful, String host, long timestamp, Long responseTimeMillis, Integer responseCode) {
        super(isSuccessful, host, timestamp);
        this.responseTimeMillis = responseTimeMillis;
        this.responseCode = responseCode;
    }

    public Long getResponseTimeMillis() {
        return responseTimeMillis;
    }

    public Integer getResponseCode() {
        return responseCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TCPPingResult) obj;
        return this.isSuccessful == that.isSuccessful &&
                Objects.equals(this.host, that.host) &&
                Objects.equals(this.responseTimeMillis, that.responseTimeMillis) &&
                Objects.equals(this.responseCode, that.responseCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isSuccessful, host, responseTimeMillis, responseCode);
    }

    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("isSuccessful", isSuccessful);
        jsonObject.put("host", host);
        jsonObject.put("timeStamp", timeStamp);
        if (responseTimeMillis != null) {
            jsonObject.put("responseTimeMillis", responseTimeMillis);
        }
        if (responseCode != null) {
            jsonObject.put("responseCode", responseCode);
        }
        return jsonObject.toString();
    }
}
