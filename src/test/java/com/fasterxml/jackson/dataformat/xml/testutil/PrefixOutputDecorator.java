package com.fasterxml.jackson.dataformat.xml.testutil;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.io.OutputDecorator;

@SuppressWarnings("serial")
public class PrefixOutputDecorator extends OutputDecorator
{
    protected final byte[] _prefix;

    public PrefixOutputDecorator(byte[] p) {
        _prefix = p;
    }

    @Override
    public OutputStream decorate(IOContext ctxt, OutputStream out)
            throws IOException
    {
        if (out instanceof BufferedOut) {
            throw new IllegalStateException("Trying to decorate `Buffered` (double-decoration!)");
        }
        return new BufferedOut(out, _prefix);
    }

    @Override
    public Writer decorate(IOContext ctxt, Writer w) throws IOException {
        for (byte b : _prefix) {
            w.write((char) (b & 0xFF));
        }
        return w;
    }

    static class BufferedOut extends FilterOutputStream {
        protected byte[] _prefix;

        public BufferedOut(OutputStream b, byte[] prefix) {
            super(b);
            _prefix = prefix;
        }

        @Override
        public void write(int b) throws IOException {
            if (_prefix != null) {
                out.write(_prefix);
                _prefix = null;
            }
            super.write(b);
        }

        @Override
        public void write(byte[] b, int offset, int len) throws IOException {
            if (_prefix != null) {
                out.write(_prefix);
                _prefix = null;
            }
            super.write(b, offset, len);
        }
    }
}
