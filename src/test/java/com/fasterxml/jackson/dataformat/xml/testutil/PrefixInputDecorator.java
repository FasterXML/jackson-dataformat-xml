package com.fasterxml.jackson.dataformat.xml.testutil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.SequenceInputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.io.InputDecorator;

@SuppressWarnings("serial")
public class PrefixInputDecorator extends InputDecorator
{
    protected final byte[] _prefix;

    public PrefixInputDecorator(byte[] p) {
        _prefix = p;
    }

    @Override
    public InputStream decorate(IOContext ctxt, InputStream in) {
        if (in instanceof MySequenceInputStream) {
            throw new IllegalStateException("Trying to decorate MySequenceInputStream (double-decoration!)");
        }
        return new MySequenceInputStream(new ByteArrayInputStream(_prefix), in);
    }

    @Override
    public InputStream decorate(IOContext ctxt, byte[] src, int offset, int length) {
        return decorate(ctxt, new ByteArrayInputStream(src, offset, length));
    }

    @Override
    public Reader decorate(IOContext ctxt, Reader r) throws IOException {
        if (r instanceof SequenceReader) {
            throw new IllegalStateException("Trying to decorate SequenceReader (double-decoration!)");
        }
        String str = new String(_prefix, StandardCharsets.UTF_8);
        return new SequenceReader(new StringReader(str), r);
    }

    // sub-class only so we can check for "double decoration"
    static class MySequenceInputStream extends SequenceInputStream {
        public MySequenceInputStream(InputStream in1, InputStream in2) {
            super(in1, in2);
        }
    }

    static class SequenceReader extends Reader {
        protected Reader _reader1, _reader2;

        public SequenceReader(Reader r1, Reader r2) {
            _reader1 = r1;
            _reader2 = r2;
        }

        @Override
        public int read(char[] cbuf, int off, int len) throws IOException {
            if (_reader1 != null) {
                int count = _reader1.read(cbuf, off, len);
                if (count > 0) {
                    return count;
                }
                _reader1 = null;
            }
            if (_reader2 != null) {
                int count = _reader2.read(cbuf, off, len);
                if (count > 0) {
                    return count;
                }
                _reader2 = null;
            }
            return -1;
        }

        @Override
        public void close() throws IOException {
            _reader1 = _reader2 = null;
        }
    }
}
