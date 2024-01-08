package com.sample.spring.logging;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class CacheServletInputStream extends ServletInputStream {

    private final InputStream inputStream;

    public CacheServletInputStream(ByteArrayInputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public boolean isFinished() {
        try {
            return this.inputStream.available() == 0;
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    @Override
    public int available() throws IOException {
        return this.inputStream.available();
    }

    @Override
    public int read() throws IOException {
        return this.inputStream.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return inputStream.read(b, off, len);
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }

    @Override
    public synchronized void reset() throws IOException {
        inputStream.reset();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return inputStream.read(b);
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setReadListener(ReadListener readListener) {
        throw new UnsupportedOperationException();
    }
}