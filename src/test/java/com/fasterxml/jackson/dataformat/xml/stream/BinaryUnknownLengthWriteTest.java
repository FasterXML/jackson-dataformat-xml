package com.fasterxml.jackson.dataformat.xml.stream;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.namespace.QName;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.Base64Variants;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestUtil;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

// [dataformat-xml#894]: `JsonGenerator.writeBinary(InputStream, dataLength)` documents a
// negative `dataLength` as "length unknown, read to end" (the JSON backend honors it);
// verify the XML backend streams to end instead of failing with a raw runtime exception.
public class BinaryUnknownLengthWriteTest extends XmlTestUtil
{
    // Stream that hands out a single byte per read() call
    static class ShortReadInputStream extends FilterInputStream
    {
        ShortReadInputStream(InputStream in) { super(in); }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return super.read(b, off, Math.min(len, 1));
        }
    }

    private final XmlMapper MAPPER = newMapper();

    private final byte[] DATA = utf8Bytes("hello, binary world");
    // base64 of DATA
    private final String ENCODED = "aGVsbG8sIGJpbmFyeSB3b3JsZA==";

    @Test
    public void testElementUnknownLength() throws Exception
    {
        StringWriter out = new StringWriter();
        try (ToXmlGenerator gen = (ToXmlGenerator) MAPPER.createGenerator(out)) {
            gen.setNextName(new QName("root"));
            gen.writeStartObject();
            gen.writeFieldName("bin");
            assertEquals(DATA.length,
                    gen.writeBinary(Base64Variants.MIME, new ByteArrayInputStream(DATA), -1));
            gen.writeEndObject();
        }
        assertEquals("<root><bin>" + ENCODED + "</bin></root>", removeSjsxpNamespace(out.toString()));
    }

    @Test
    public void testAttributeUnknownLength() throws Exception
    {
        StringWriter out = new StringWriter();
        try (ToXmlGenerator gen = (ToXmlGenerator) MAPPER.createGenerator(out)) {
            gen.setNextName(new QName("root"));
            gen.writeStartObject();
            gen.setNextIsAttribute(true);
            gen.writeFieldName("bin");
            assertEquals(DATA.length,
                    gen.writeBinary(Base64Variants.MIME, new ByteArrayInputStream(DATA), -1));
            gen.writeEndObject();
        }
        assertEquals("<root bin=\"" + ENCODED + "\"/>", removeSjsxpNamespace(out.toString()));
    }

    @Test
    public void testUnwrappedUnknownLength() throws Exception
    {
        StringWriter out = new StringWriter();
        try (ToXmlGenerator gen = (ToXmlGenerator) MAPPER.createGenerator(out)) {
            gen.setNextName(new QName("root"));
            gen.writeStartObject();
            gen.writeFieldName("bin");
            gen.setNextIsUnwrapped(true);
            assertEquals(DATA.length,
                    gen.writeBinary(Base64Variants.MIME, new ByteArrayInputStream(DATA), -1));
            gen.writeEndObject();
        }
        assertEquals("<root>" + ENCODED + "</root>", removeSjsxpNamespace(out.toString()));
    }

    @Test
    public void testPrettyPrintedUnknownLength() throws Exception
    {
        StringWriter out = new StringWriter();
        try (ToXmlGenerator gen = (ToXmlGenerator) MAPPER.writerWithDefaultPrettyPrinter()
                .createGenerator(out)) {
            gen.setNextName(new QName("root"));
            gen.writeStartObject();
            gen.writeFieldName("bin");
            assertEquals(DATA.length,
                    gen.writeBinary(Base64Variants.MIME, new ByteArrayInputStream(DATA), -1));
            gen.writeEndObject();
        }
        assertEquals("<root>\n  <bin>" + ENCODED + "</bin>\n</root>\n",
                removeSjsxpNamespace(out.toString()));
    }

    // Empty stream should render the same whether length is given as 0 or unknown
    @Test
    public void testEmptyStream() throws Exception
    {
        final byte[] empty = new byte[0];
        assertEquals("<root><bin/></root>", _writeElement(Base64Variants.MIME, empty, 0));
        assertEquals("<root><bin/></root>", _writeElement(Base64Variants.MIME, empty, -1));
        assertEquals("<root bin=\"\"/>", _writeAttribute(Base64Variants.MIME, empty, 0));
        assertEquals("<root bin=\"\"/>", _writeAttribute(Base64Variants.MIME, empty, -1));
    }

    // Payload bigger than the recycled read buffer, and not a multiple of 3, so that
    // partial triplets have to be carried over between reads
    @Test
    public void testLargePayloadUnknownLength() throws Exception
    {
        final byte[] big = new byte[7001];
        for (int i = 0; i < big.length; i++) {
            big[i] = (byte) i;
        }
        // Use variant without line feeds: the Stax2 writer starts a new line counter
        // for each chunk written, so with MIME only the line break positions would differ
        final Base64Variant b64v = Base64Variants.MIME_NO_LINEFEEDS;
        final String expected = _writeElement(b64v, big);
        final String actual = _writeElement(b64v, big, -1);
        assertEquals(expected, actual);
        assertEquals(expected, _writeElement(b64v, big, big.length));
        assertEquals(_writeAttribute(b64v, big, big.length), _writeAttribute(b64v, big, -1));

        // and finally, make sure it decodes back
        String encoded = actual.substring("<root><bin>".length(), actual.length() - "</bin></root>".length());
        assertArrayEquals(big, b64v.decode(encoded));
    }

    @Test
    public void testShortReadsUnknownLength() throws Exception
    {
        StringWriter out = new StringWriter();
        try (ToXmlGenerator gen = (ToXmlGenerator) MAPPER.createGenerator(out)) {
            gen.setNextName(new QName("root"));
            gen.writeStartObject();
            gen.writeFieldName("bin");
            assertEquals(DATA.length, gen.writeBinary(Base64Variants.MIME,
                    new ShortReadInputStream(new ByteArrayInputStream(DATA)), -1));
            gen.writeEndObject();
        }
        assertEquals("<root><bin>" + ENCODED + "</bin></root>", removeSjsxpNamespace(out.toString()));
    }

    /*
    /**********************************************************************
    /* Helper methods
    /**********************************************************************
     */

    private String _writeElement(Base64Variant b64v, byte[] data) throws Exception
    {
        StringWriter out = new StringWriter();
        try (ToXmlGenerator gen = (ToXmlGenerator) MAPPER.createGenerator(out)) {
            gen.setNextName(new QName("root"));
            gen.writeStartObject();
            gen.writeFieldName("bin");
            gen.writeBinary(b64v, data, 0, data.length);
            gen.writeEndObject();
        }
        return removeSjsxpNamespace(out.toString());
    }

    private String _writeElement(Base64Variant b64v, byte[] data, int dataLength) throws Exception
    {
        StringWriter out = new StringWriter();
        try (ToXmlGenerator gen = (ToXmlGenerator) MAPPER.createGenerator(out)) {
            gen.setNextName(new QName("root"));
            gen.writeStartObject();
            gen.writeFieldName("bin");
            assertEquals(data.length,
                    gen.writeBinary(b64v, new ByteArrayInputStream(data), dataLength));
            gen.writeEndObject();
        }
        return removeSjsxpNamespace(out.toString());
    }

    private String _writeAttribute(Base64Variant b64v, byte[] data, int dataLength) throws Exception
    {
        StringWriter out = new StringWriter();
        try (ToXmlGenerator gen = (ToXmlGenerator) MAPPER.createGenerator(out)) {
            gen.setNextName(new QName("root"));
            gen.writeStartObject();
            gen.setNextIsAttribute(true);
            gen.writeFieldName("bin");
            assertEquals(data.length,
                    gen.writeBinary(b64v, new ByteArrayInputStream(data), dataLength));
            gen.writeEndObject();
        }
        return removeSjsxpNamespace(out.toString());
    }
}
