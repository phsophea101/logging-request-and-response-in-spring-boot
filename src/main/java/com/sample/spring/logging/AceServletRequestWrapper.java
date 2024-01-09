package com.sample.spring.logging;

import com.sample.spring.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
public class AceServletRequestWrapper extends HttpServletRequestWrapper {

    private ByteArrayOutputStream cachedBytes;
    private Map<String, String> headers;
    private Map<String, String[]> parameters;

    public AceServletRequestWrapper(HttpServletRequest request) {
        super(request);
        this.parameters = new HashMap<>();
        this.headers = new HashMap<>();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (cachedBytes == null) {
            cachedBytes = new ByteArrayOutputStream();
            StreamUtils.copy(super.getInputStream(), cachedBytes);
        }
        return new CacheServletInputStream(new ByteArrayInputStream(cachedBytes.toByteArray()));
    }

    @Override
    public String getHeader(String name) {
        if (StringUtils.isEmpty(name)) return null;
        String headerValue = headers.get(name);
        return headerValue != null ? headerValue : super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        Set<String> set = new HashSet<>(headers.keySet());
        Enumeration<String> headers = super.getHeaderNames();
        while (headers.hasMoreElements())
            set.add(headers.nextElement());
        return Collections.enumeration(set);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        Set<String> set = new HashSet<>(Collections.list(super.getHeaders(name)));
        if (headers.containsKey(name))
            set.add(headers.get(name));
        return Collections.enumeration(set);
    }

    @Override
    public String getParameter(String name) {
        String[] strings = parameters.get(name);
        if (strings != null)
            return strings[0];
        return super.getParameter(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> temp = new HashMap<>(parameters);
        temp.putAll(super.getParameterMap());
        return Collections.unmodifiableMap(temp);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(getParameterMap().keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        return getParameterMap().get(name);
    }

    public boolean isQueryParam() {
        return !getParameterMap().isEmpty();
    }

    public String getContent() {
        try {
            if (!MediaType.MULTIPART_FORM_DATA_VALUE.equalsIgnoreCase(getContentType()))
                return IOUtils.toString(getInputStream(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("exception occurred while read request body {}", e.getMessage());
        }
        return "";
    }

    public String getQueryParam() {
        StringBuilder builder = new StringBuilder();
        if (!MediaType.MULTIPART_FORM_DATA_VALUE.equalsIgnoreCase(getContentType())) {
            Map<String, String[]> params = getParameterMap();
            for (Map.Entry<String, String[]> entry : params.entrySet()) {
                for (String value : entry.getValue()) {
                    String values = entry.getKey() + "=" + value + "&";
                    builder.append(values);
                }
            }
        }
        return StringUtil.substringLast(builder.toString());
    }

    public Object getPathVariable() {
        return getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
    }

    public void putParams(String name, String... value) {
        parameters.put(name, value);
    }

    public void addHeader(String name, String value) {
        headers.put(name, value);
    }

    public WebRequest getWeRequest() {
        return new ServletWebRequest(this);
    }
}