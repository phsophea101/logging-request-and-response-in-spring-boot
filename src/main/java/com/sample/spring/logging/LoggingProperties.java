package com.sample.spring.logging;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.Ordered;
import org.springframework.util.AntPathMatcher;

@ConfigurationProperties(prefix = "custom.logging")
public class LoggingProperties {

    public static final String DEFAULT_EXCLUDE_REQUEST_HEADER = "x-auth-data,auth-data,x-auth,downstream-redirect";
    public static String NUMBERS_AND_ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static AntPathMatcher ANT_PATH_MATCHER = new AntPathMatcher();
    public static final String ACTUATOR_MEDIA_TYPE_V1_JSON = "application/vnd.spring-boot.actuator.v1+json";
    public static final String UTF_8 = "UTF-8";
    public static final String ACTUATOR_MEDIA_TYPE_V2_JSON = "application/vnd.spring-boot.actuator.v2+json";
    public static String[] IP_HEADER_CANDIDATES = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"};
    private boolean enable = false;
    private boolean includeResponseBody = true;
    private boolean includeRequestBody = true;
    private boolean includeResponseHeader = true;
    private boolean includeRequestHeader = true;
    private String excludeRequestHeader = DEFAULT_EXCLUDE_REQUEST_HEADER;
    private String excludeResponseHeader = "";
    private int maxRequest = 2048;
    private int maxResponse = 2048;
    private boolean ignoreRequest = false;
    private boolean ignoreResponse = false;
    private String ignoreAntMatches = "";
    private int traceIdLength = 10;
    private int order = Ordered.HIGHEST_PRECEDENCE;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public boolean isIncludeResponseBody() {
        return includeResponseBody;
    }

    public void setIncludeResponseBody(boolean includeResponseBody) {
        this.includeResponseBody = includeResponseBody;
    }

    public boolean isIncludeRequestBody() {
        return includeRequestBody;
    }

    public void setIncludeRequestBody(boolean includeRequestBody) {
        this.includeRequestBody = includeRequestBody;
    }

    public boolean isIncludeResponseHeader() {
        return includeResponseHeader;
    }

    public void setIncludeResponseHeader(boolean includeResponseHeader) {
        this.includeResponseHeader = includeResponseHeader;
    }

    public boolean isIncludeRequestHeader() {
        return includeRequestHeader;
    }

    public void setIncludeRequestHeader(boolean includeRequestHeader) {
        this.includeRequestHeader = includeRequestHeader;
    }

    public int getMaxRequest() {
        return maxRequest;
    }

    public void setMaxRequest(int maxRequest) {
        this.maxRequest = maxRequest;
    }

    public int getMaxResponse() {
        return maxResponse;
    }

    public void setMaxResponse(int maxResponse) {
        this.maxResponse = maxResponse;
    }

    public boolean isIgnoreRequest() {
        return ignoreRequest;
    }

    public void setIgnoreRequest(boolean ignoreRequest) {
        this.ignoreRequest = ignoreRequest;
    }

    public boolean isIgnoreResponse() {
        return ignoreResponse;
    }

    public void setIgnoreResponse(boolean ignoreResponse) {
        this.ignoreResponse = ignoreResponse;
    }

    public String getIgnoreAntMatches() {
        return ignoreAntMatches;
    }

    public void setIgnoreAntMatches(String ignoreAntMatches) {
        this.ignoreAntMatches = ignoreAntMatches;
    }

    public int getTraceIdLength() {
        return traceIdLength;
    }

    public void setTraceIdLength(int traceIdLength) {
        this.traceIdLength = traceIdLength;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getExcludeRequestHeader() {
        return excludeRequestHeader;
    }

    public void setExcludeRequestHeader(String excludeRequestHeader) {
        this.excludeRequestHeader = excludeRequestHeader;
    }

    public String getExcludeResponseHeader() {
        return excludeResponseHeader;
    }

    public void setExcludeResponseHeader(String excludeResponseHeader) {
        this.excludeResponseHeader = excludeResponseHeader;
    }
}