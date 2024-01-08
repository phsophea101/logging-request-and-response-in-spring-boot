package com.sample.spring.logging;

import com.sample.spring.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

@Slf4j
public class ServletContext implements Serializable {

    private static final long serialVersionUID = -6079925008310125442L;

    private final String uuid;
    private String ip;
    private String endpoint;
    private String method;
    private String requestBody;
    private String queryParam;
    private String responseBody;
    private int maxRequest;
    private int maxResponse;
    private final MultiValueMap<String, String> requestHeader = new LinkedMultiValueMap<>();
    private final MultiValueMap<String, String> responseHeader = new LinkedMultiValueMap<>();

    public ServletContext() {
        uuid = StringUtil.random(8, LoggingProperties.NUMBERS_AND_ALPHABET);
    }

    public ServletContext(int maxRequest, int maxResponse, int traceIdLength) {
        uuid = StringUtil.random(traceIdLength, LoggingProperties.NUMBERS_AND_ALPHABET);
        this.maxRequest = maxRequest;
        this.maxResponse = maxResponse;
    }

    public String getUuid() {
        return uuid;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(HttpServletRequest request) {
        for (String header : LoggingProperties.IP_HEADER_CANDIDATES) {
            String requestId = request.getHeader(header);
            if (StringUtils.isNotEmpty(requestId) && !"unknown".equalsIgnoreCase(ip)) {
                this.ip = requestId;
                break;
            }
        }
        if (StringUtils.isEmpty(ip))
            this.ip = request.getRemoteAddr();
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setMaxRequest(Integer maxRequest) {
        this.maxRequest = maxRequest;
    }

    public void setMaxResponse(Integer maxResponse) {
        this.maxResponse = maxResponse;
    }

    public Integer getMaxRequest() {
        return maxRequest;
    }

    public Integer getMaxResponse() {
        return maxResponse;
    }

    public void setRequestHeader(HttpServletRequest request, List<String> excludeRequestHeader) {
        Enumeration<String> headers = request.getHeaderNames();
        while (headers.hasMoreElements()) {
            String name = headers.nextElement();
            if (!excludeRequestHeader.contains(name)) {
                requestHeader.add(name, request.getHeader(name));
            }
        }
    }

    public MultiValueMap<String, String> getRequestHeader() {
        return requestHeader;
    }

    public void setResponseHeader(HttpServletResponse response, List<String> excludeResponseHeader) {
        Collection<String> headers = response.getHeaderNames();
        for (String name : headers) {
            if (!excludeResponseHeader.contains(name)) {
                responseHeader.add(name, response.getHeader(name));
            }
        }
    }

    public MultiValueMap<String, String> getResponseHeader() {
        return responseHeader;
    }

    public String getEndpoint() {
        return StringUtils.isEmpty(endpoint) ? "unknown endpoint" : endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getRequestBody() {
        try {
            if (StringUtils.isEmpty(requestBody))
                return "";
            else if (requestBody.startsWith("{"))
                return new JSONObject(requestBody).toString();
            else if (requestBody.startsWith("["))
                return new JSONArray(requestBody).toString();
        } catch (Exception e) {
            log.warn("exception occurred while build log request body {}", e.getMessage());
        }
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public String getResponseBody() {
        try {
            if (StringUtils.isEmpty(responseBody))
                return "";
            else if (responseBody.startsWith("{"))
                return new JSONObject(responseBody).toString();
            else if (responseBody.startsWith("["))
                return new JSONArray(responseBody).toString();
        } catch (Exception e) {
            log.warn("exception occurred while build log response body {}", e.getMessage());
        }
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public String getQueryParam() {
        return queryParam;
    }

    public void setQueryParam(String queryParam) {
        this.queryParam = queryParam;
    }

    public String buildLogRequest() {
        StringBuilder builder = new StringBuilder("REQUEST ");
        builder.append(getMethod()).append(" ");
        builder.append(getUuid()).append(", ");
        builder.append(getEndpoint()).append(", ");
        builder.append(getIp());
        String header = buildHeader(requestHeader);
        if (StringUtils.isNotEmpty(header))
            builder.append(", ").append(header);
        if (StringUtils.isNotEmpty(queryParam))
            builder.append(", parameter:").append(queryParam);
        boolean isMaxRequest = truncateBody(getRequestBody(), builder, maxRequest);
        if (isMaxRequest) log.info("Truncated request body length longer than " + maxRequest);
        return builder.toString();
    }

    public String buildLogResponse(Integer status) {
        StringBuilder builder = new StringBuilder("RESPONSE ");
        builder.append(status).append(" ");
        builder.append(getUuid()).append(", ");
        builder.append(getEndpoint()).append(", ");
        builder.append(getIp());
        String header = buildHeader(responseHeader);
        if (StringUtils.isNotEmpty(header))
            builder.append(", ").append(header);
        boolean isMaxResponse = truncateBody(getResponseBody(), builder, maxResponse);
        if (isMaxResponse) log.info("Truncated response body length longer than " + maxResponse);
        return builder.toString();
    }

    public static String buildHeader(MultiValueMap<String, String> headers) {
        if (headers == null || headers.isEmpty()) return null;
        StringBuilder builder = new StringBuilder("headers:");
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            StringBuilder values = new StringBuilder(entry.getKey()).append("[");
            for (String value : entry.getValue())
                values.append(value).append(",");
            if (entry.getValue().isEmpty())
                builder.append(values.append("]"));
            else
                builder.append(values.length() > 0 ? StringUtil.substringLast(values.toString()) : "").append("]");
        }
        return builder.toString();
    }

    public static boolean truncateBody(String body, StringBuilder builder, Integer maxLength) {
        String temp = body;
        boolean max = false;
        if (StringUtils.isNotEmpty(temp)) {
            if (temp.length() > maxLength) {
                temp = temp.substring(0, maxLength);
                max = true;
            }
            builder.append(", body:").append(temp.trim());
        }
        return max;
    }
}
