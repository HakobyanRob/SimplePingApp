package com.github.akopyanrob.service;

import com.github.akopyanrob.result.PingResult;

public interface PingService {

    PingResult ping(String host);
}
