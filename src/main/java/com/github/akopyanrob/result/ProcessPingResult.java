package com.github.akopyanrob.result;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class ProcessPingResult extends PingResult {
    private final List<String> resultLines;

    protected ProcessPingResult(boolean isSuccessful, String host, long timeStamp, List<String> resultLines) {
        super(isSuccessful, host, timeStamp);
        this.resultLines = resultLines;
    }

    public List<String> getResultLines() {
        return resultLines;
    }

    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("isSuccessful", isSuccessful);
        jsonObject.put("host", host);
        jsonObject.put("timeStamp", timeStamp);

        JSONArray resultLinesJson = new JSONArray(resultLines);
        jsonObject.put("resultLines", resultLinesJson);

        return jsonObject.toString();
    }
}
