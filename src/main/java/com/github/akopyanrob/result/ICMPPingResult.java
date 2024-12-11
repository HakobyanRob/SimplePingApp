package com.github.akopyanrob.result;

import java.util.List;

public class ICMPPingResult extends ProcessPingResult {

    public ICMPPingResult(boolean isSuccessful, String host, long timeStamp, List<String> resultLines) {
        super(isSuccessful, host, timeStamp, resultLines);
    }
}
