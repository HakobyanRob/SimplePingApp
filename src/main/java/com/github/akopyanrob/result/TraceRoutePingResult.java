package com.github.akopyanrob.result;

import java.util.List;

public class TraceRoutePingResult extends ProcessPingResult {

    public TraceRoutePingResult(boolean isSuccessful, String host, long timeStamp, List<String> resultLines) {
        super(isSuccessful, host, timeStamp, resultLines);
    }
}
