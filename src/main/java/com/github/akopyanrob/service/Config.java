package com.github.akopyanrob.service;

import java.util.Objects;

public record Config(String[] hosts, int pingDelay, int timeout) {
    public Config {
        Objects.requireNonNull(hosts);
        if (pingDelay < 0 || timeout < 0) {
            throw new IllegalArgumentException("Ping delay and timeout must be non-negative");
        }
    }
}
