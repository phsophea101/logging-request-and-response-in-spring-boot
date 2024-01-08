package com.sample.spring.logging;

import org.apache.commons.io.output.TeeOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import java.io.*;

public class CacheServletOutputStream extends ServletOutputStream {

    private final OutputStream copier;
    private final ServletOutputStream outputStream;

    public CacheServletOutputStream(ServletOutputStream outputStream, OutputStream branch) {
        this.copier = new TeeOutputStream(outputStream, branch);
        this.outputStream = outputStream;
    }

    @Override
    public void write(byte[] arg0, int arg1, int arg2) throws IOException {
        copier.write(arg0, arg1, arg2);
    }

    @Override
    public void write(int b) throws IOException {
        copier.write(b);
    }

    @Override
    public boolean isReady() {
        return outputStream.isReady();
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {
        outputStream.setWriteListener(writeListener);
    }

    static class ResponsePrintWriter extends PrintWriter {

        public ResponsePrintWriter(OutputStream content, String encoding) throws UnsupportedEncodingException {
            super(new OutputStreamWriter(content, encoding));
        }

        @Override
        public void write(int c) {
            super.write(c);
            super.flush();
        }

        @Override
        public void write(char[] buf, int off, int len) {
            super.write(buf, off, len);
            super.flush();
        }

        @Override
        public void write(String s, int off, int len) {
            super.write(s, off, len);
            super.flush();
        }
    }
}
