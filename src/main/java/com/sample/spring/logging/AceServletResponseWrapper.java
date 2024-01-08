package com.sample.spring.logging;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.util.FastByteArrayOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class AceServletResponseWrapper extends HttpServletResponseWrapper {

    private ServletOutputStream cachedServlet;
    private PrintWriter printWriter;
    private final FastByteArrayOutputStream content = new FastByteArrayOutputStream(1024);

    public AceServletResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (cachedServlet == null)
            cachedServlet = new CacheServletOutputStream(getResponse().getOutputStream(), content);
        return cachedServlet;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (printWriter == null)
            printWriter = new CacheServletOutputStream.ResponsePrintWriter(getOutputStream(), LoggingProperties.UTF_8);
        return printWriter;
    }

    /*@Override
    public void flushBuffer() throws IOException {
        if (cachedServlet != null)
            cachedServlet.flush();
    }*/

    @Override
    public void setContentLength(int len) {
        if (len > this.content.size())
            this.content.resize(len);
    }

    @Override
    public void setContentLengthLong(long len) {
        if (len > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Content-Length exceeds ContentCachingResponseWrapper's maximum (" +
                    Integer.MAX_VALUE + "): " + len);
        }
        int lenInt = (int) len;
        if (lenInt > content.size())
            content.resize(lenInt);
    }

    @Override
    public void setBufferSize(int size) {
        if (size > content.size())
            content.resize(size);
    }

    @Override
    public void resetBuffer() {
        content.reset();
    }

    @Override
    public void reset() {
        super.reset();
        content.reset();
    }

    public String getContent() {
        String contentType = getContentType();
        boolean isJsonBody = StringUtils.isNotEmpty(contentType)
                && (contentType.equalsIgnoreCase(MediaType.APPLICATION_JSON_VALUE)
                || contentType.equalsIgnoreCase(MediaType.APPLICATION_JSON_UTF8_VALUE)
                || contentType.startsWith(LoggingProperties.ACTUATOR_MEDIA_TYPE_V1_JSON)
                || contentType.startsWith(LoggingProperties.ACTUATOR_MEDIA_TYPE_V2_JSON));
        return isJsonBody ? IOUtils.toString(content.toByteArray(), StandardCharsets.UTF_8.name()) : "";
    }
}