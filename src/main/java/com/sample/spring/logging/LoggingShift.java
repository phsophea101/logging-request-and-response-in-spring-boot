package com.sample.spring.logging;

import java.util.List;

public interface LoggingShift {

    boolean changeMaxRequest(int value);

    boolean changeMaxResponse(int value);

    boolean changeTraceLength(int value);

    boolean changeExcludeRequestHeader(List<String> headers);

    boolean changeExcludeResponseHeader(List<String> headers);
}
