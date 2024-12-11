package com.github.akopyanrob.service;

import com.github.akopyanrob.result.PingResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class ProcessPingService implements PingService {

    private final ProcessBuilder processBuilder;

    protected ProcessPingService(ProcessBuilder processBuilder) {
        this.processBuilder = Objects.requireNonNull(processBuilder, "processBuilder must not be null");
    }

    @Override
    public PingResult ping(String host) {
        List<String> outputLines;
        long startTime = System.currentTimeMillis();
        try {
            outputLines = executeCommand(getPingCommand(host));
        } catch (IOException | InterruptedException e) {
            return handleError(host, startTime, e.getMessage());
        }

        boolean success = parsePingOutput(outputLines);
        return createPingResult(success, host, startTime, outputLines);
    }

    protected abstract String getPingCommand(String host);

    protected abstract boolean parsePingOutput(List<String> outputLines);

    protected abstract PingResult createPingResult(boolean success, String host, long startTime, List<String> outputLines);

    protected abstract PingResult handleError(String host, long startTime, String errorMessage);

    private List<String> executeCommand(String command) throws IOException, InterruptedException {
        List<String> outputLines = new ArrayList<>();
        processBuilder.command(command.split(" "));
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();
        var reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.isBlank()) {
                outputLines.add(line.trim());
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Command execution failed with exit code: " + exitCode);
        }
        return outputLines;
    }
}
