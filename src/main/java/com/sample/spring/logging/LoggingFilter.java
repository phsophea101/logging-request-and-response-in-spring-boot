package com.sample.spring.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sample.spring.concurrent.InheritableContextHolder;
import com.sample.spring.concurrent.ThreadContextHolder;
import com.sample.spring.config.SmartLocaleResolver;
import com.sample.spring.exception.BizException;
import com.sample.spring.util.ContextUtil;
import com.sample.spring.util.I18nUtils;
import com.sample.spring.web.vo.response.ResponseErrorVo;
import com.sample.spring.web.vo.response.ResponseVO;
import com.sample.spring.web.vo.response.ResponseVOBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONObject;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class LoggingFilter extends OncePerRequestFilter implements Ordered, LoggingShift {

    private final AntPathMatchUrl antPathMatch = new AntPathMatchUrl();
    private Boolean includeResponseBody = true;
    private Boolean includeRequestBody = true;
    private Boolean includeResponseHeader = false;
    private Boolean includeRequestHeader = true;
    private List<String> excludeRequestHeader = new ArrayList<>();
    private List<String> excludeResponseHeader = new ArrayList<>();
    private int maxRequest = 2048;
    private int maxResponse = 2048;
    private int traceIdLength = 10;
    private int order = Ordered.HIGHEST_PRECEDENCE;


    public LoggingFilter ignoreAntPathMatch(String... match) {
        antPathMatch.addMatching(match);
        return this;
    }

    public LoggingFilter ignoreRequest(boolean ignore) {
        antPathMatch.setIgnoreRequest(ignore);
        return this;
    }

    public LoggingFilter ignoreResponse(boolean ignore) {
        antPathMatch.setIgnoreResponse(ignore);
        return this;
    }

    public LoggingFilter includeResponseBody(boolean includeResponseBody) {
        this.includeResponseBody = includeResponseBody;
        return this;
    }

    public LoggingFilter includeRequestBody(boolean includeRequestBody) {
        this.includeRequestBody = includeRequestBody;
        return this;
    }

    public LoggingFilter includeResponseHeader(boolean includeResponseHeader) {
        this.includeResponseHeader = includeResponseHeader;
        return this;
    }

    public LoggingFilter includeRequestHeader(Boolean includeRequestHeader) {
        this.includeRequestHeader = includeRequestHeader;
        return this;
    }

    public LoggingFilter maxRequest(int max) {
        this.maxRequest = max;
        return this;
    }

    public LoggingFilter maxResponse(int max) {
        this.maxResponse = max;
        return this;
    }

    public LoggingFilter setTraceIdLength(int traceIdLength) {
        this.traceIdLength = traceIdLength;
        return this;
    }

    public LoggingFilter setOrder(int order) {
        this.order = order;
        return this;
    }

    public LoggingFilter setExcludeRequestHeader(List<String> headers) {
        this.excludeRequestHeader = headers;
        return this;
    }

    public LoggingFilter setExcludeResponseHeader(List<String> headers) {
        this.excludeResponseHeader = headers;
        return this;
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException {
        SmartLocaleResolver.validateLocale(request);
        AceServletRequestWrapper requestWrapper = new AceServletRequestWrapper(request);
        AceServletResponseWrapper responseWrapper = new AceServletResponseWrapper(response);
        ServletContext context = new ServletContext(maxRequest, maxResponse, traceIdLength);
        try {
            context.setIp(requestWrapper);
            context.setMethod(requestWrapper.getMethod());
            context.setEndpoint(requestWrapper.getRequestURI());
            context.setQueryParam(requestWrapper.getQueryParam());
            if (Boolean.TRUE.equals(includeRequestHeader))
                context.setRequestHeader(requestWrapper, excludeRequestHeader);
            if (Boolean.TRUE.equals(includeRequestBody))
                context.setRequestBody(requestWrapper.getContent());
            if (!antPathMatch.isIgnoreRequest(context.getEndpoint())) {
                log.info(context.buildLogRequest());
            }
            chain.doFilter(requestWrapper, responseWrapper);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            handleException(requestWrapper, responseWrapper, ex);
        } finally {
            ThreadContextHolder.clear();
            InheritableContextHolder.clear();
            if (Boolean.TRUE.equals(includeResponseHeader))
                context.setResponseHeader(responseWrapper, excludeResponseHeader);
            if (Boolean.TRUE.equals(includeResponseBody))
                context.setResponseBody(responseWrapper.getContent());
            if (!antPathMatch.isIgnoreResponse(context.getEndpoint())) {
                log.info(context.buildLogResponse(responseWrapper.getStatus()));
            }
        }
    }

    protected void handleException(AceServletRequestWrapper requestWrapper, AceServletResponseWrapper responseWrapper, Exception ex) throws IOException {
        String message = I18nUtils.messageResolver("unexpected.error", "Unexpected error");
        String code = "E0500";
        Throwable cause = ExceptionUtils.getRootCause(ex);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        Optional<ObjectMapper> beanMapper = ContextUtil.optBean(ObjectMapper.class);
        if (beanMapper.isEmpty())
            return;
        try {
            if (cause instanceof BizException) {
                message = ((BizException) cause).getError().getDescription();
                status = HttpStatus.BAD_REQUEST;
                code = ((BizException) cause).getError().getValue();
            } else if (cause instanceof HttpStatusCodeException) {
                status = ((HttpStatusCodeException) cause).getStatusCode();
                String body = ((HttpStatusCodeException) cause).getResponseBodyAsString();
                message = cause.getMessage();
                if (StringUtils.isNotEmpty(body) && body.startsWith("{")) {
                    JSONObject json = new JSONObject(body);
                    JSONObject error = json.getJSONObject("error");
                    message = error.optString("message", message);
                    code = error.optString("code", "E0400");
                }
            } else if (ex instanceof RuntimeException || ex instanceof ServletException) {
                message = StringUtils.isEmpty(ex.getMessage()) ? I18nUtils.messageResolver("something.went.wrong", "Something went wrong") : ex.getMessage();
                status = HttpStatus.EXPECTATION_FAILED;
                code = String.format("E0%s", status.value());
            }
            ResponseVO<Object> response = new ResponseVOBuilder<>().status(String.valueOf(status.value())).error(new ResponseErrorVo(code, message, ex.getMessage())).build();
            responseWrapper.setContentType(MediaType.APPLICATION_JSON_VALUE);
            responseWrapper.setStatus(Integer.getInteger(response.getStatus()));
            responseWrapper.getOutputStream().write(beanMapper.get().writeValueAsString(response).getBytes());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            message = I18nUtils.messageResolver("unexpected.error", "Unexpected error");
            status = HttpStatus.EXPECTATION_FAILED;
            ResponseVO<Object> response = new ResponseVOBuilder<>().status(String.valueOf(status.value())).error(new ResponseErrorVo(code, message, ex.getMessage())).build();
            responseWrapper.setContentType(MediaType.APPLICATION_JSON_VALUE);
            responseWrapper.setStatus(Integer.getInteger(response.getStatus()));
            responseWrapper.getOutputStream().write(beanMapper.get().writeValueAsString(response).getBytes());
        }
    }

    @Override
    public boolean changeMaxRequest(int value) {
        return false;
    }

    @Override
    public boolean changeMaxResponse(int value) {
        return false;
    }

    @Override
    public boolean changeTraceLength(int value) {
        return false;
    }

    @Override
    public boolean changeExcludeRequestHeader(List<String> headers) {
        return false;
    }

    @Override
    public boolean changeExcludeResponseHeader(List<String> headers) {
        return false;
    }
}
