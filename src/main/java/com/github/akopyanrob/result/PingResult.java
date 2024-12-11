package com.github.akopyanrob.result;

public class PingResult {

    protected final boolean isSuccessful;
    protected final String host;
    protected final long timeStamp;

    protected PingResult(boolean isSuccessful, String host, long timeStamp) {
        this.isSuccessful = isSuccessful;
        this.host = host;
        this.timeStamp = timeStamp;
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public String getHost() {
        return host;
    }

    public long getTimeStamp() {
        return timeStamp;
    }
}
