package com.sample.spring.logging;

import com.sample.spring.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
public class LoggingInterceptor implements ClientHttpRequestInterceptor {


    private int maxRequest = 2048;
    private int maxResponse = 2048;
    private int traceIdLength = 10;
    private static final Set<String> DEFAULT_EXCLUDE_HEADER_REQUEST =
            new HashSet<>(Arrays.asList(LoggingProperties.DEFAULT_EXCLUDE_REQUEST_HEADER.split(",")));

    public LoggingInterceptor() {
    }

    public LoggingInterceptor(int maxRequest, int maxResponse, int traceIdLength) {
        this.maxRequest = maxRequest;
        this.maxResponse = maxResponse;
        this.traceIdLength = traceIdLength;
    }

    public void setMaxRequest(int maxRequest) {
        this.maxRequest = maxRequest;
    }

    public void setMaxResponse(int maxResponse) {
        this.maxResponse = maxResponse;
    }

    public void setTraceIdLength(int traceIdLength) {
        this.traceIdLength = traceIdLength;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        String uuid = StringUtil.random(traceIdLength, LoggingProperties.NUMBERS_AND_ALPHABET);
        logRequestData(uuid, request, body);
        ClientHttpResponse response = new ByteArrayInputStreamClientHttpResponse(execution.execute(request, body));
        logResponseData(uuid, request.getURI(), response);
        return response;
    }

    private void logRequestData(String uuid, HttpRequest request, byte[] bytes) {
        try {
            MultiValueMap<String, String> requestHeader = new LinkedMultiValueMap<>();
            HttpHeaders headers = request.getHeaders();
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                if (!DEFAULT_EXCLUDE_HEADER_REQUEST.contains(entry.getKey()))
                    requestHeader.put(entry.getKey(), entry.getValue());
            }
            String body = IOUtils.toString(Optional.ofNullable(bytes).orElse(new byte[]{}), StandardCharsets.UTF_8.name());
            String header = ServletContext.buildHeader(requestHeader);
            URI uri = request.getURI();
            StringBuilder builder = new StringBuilder();
            builder.append(request.getMethod()).append(" ").append(uuid).append(", ");
            builder.append(uri.getPath()).append(", ");
            builder.append(uri.getScheme()).append("://").append(uri.getHost());
            int port = uri.getPort();
            if (port > 0) builder.append(":").append(port);
            if (StringUtils.isNotEmpty(header)) builder.append(", ").append(header);
            boolean isMaxRequest = ServletContext.truncateBody(body, builder, maxRequest);
            log.info(uri.getScheme() + "-outgoing request {}", builder.toString());
            if (isMaxRequest)
                log.info("Truncated request body length longer than " + maxRequest);
        } catch (Exception ex) {
            log.warn("exception occurred while try to log " + request.getURI().getScheme() + " outgoing request", ex);
        }
    }

    private void logResponseData(String uuid, URI uri, ClientHttpResponse response) throws IOException {
        InputStream inputStream = response.getBody();
        try {
            MultiValueMap<String, String> responseHeader = new LinkedMultiValueMap<>();
            HttpHeaders headers = response.getHeaders();
            for (Map.Entry<String, List<String>> entry : headers.entrySet())
                responseHeader.put(entry.getKey(), entry.getValue());
            String body = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            String header = ServletContext.buildHeader(responseHeader);
            StringBuilder builder = new StringBuilder();
            builder.append(response.getRawStatusCode()).append(" ").append(uuid).append(", ");
            builder.append(uri.getPath()).append(", ");
            builder.append(uri.getScheme()).append("://").append(uri.getHost());
            int port = uri.getPort();
            if (port > 0) builder.append(":").append(port);
            if (StringUtils.isNotEmpty(header)) builder.append(", ").append(header);
            boolean isMaxResponse = ServletContext.truncateBody(body, builder, maxResponse);
            log.info(uri.getScheme() + "-outgoing response {}", builder.toString());
            if (isMaxResponse)
                log.info("Truncated response body length longer than " + maxResponse);
        } catch (Exception ex) {
            log.warn("exception occurred while try to log " + uri.getScheme() + " outgoing response", ex);
        } finally {
            if (inputStream.markSupported())
                inputStream.reset();
        }
    }
}
