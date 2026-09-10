package tools.jackson.dataformat.xml.stream;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.namespace.QName;

import org.junit.jupiter.api.Test;

import tools.jackson.core.Base64Variant;
import tools.jackson.core.Base64Variants;
import tools.jackson.core.JacksonException;
import tools.jackson.core.exc.JacksonIOException;

import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlTestUtil;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

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

    // Stream that fails partway through, to verify buffers are recycled properly
    static class FailingInputStream extends InputStream
    {
        private int _left;

        FailingInputStream(int okBytes) { _left = okBytes; }

        @Override
        public int read() throws IOException {
            byte[] b = new byte[1];
            int count = read(b, 0, 1);
            return (count < 0) ? -1 : (b[0] & 0xFF);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (_left <= 0) {
                throw new IOException("Test-induced read failure");
            }
            int count = Math.min(len, _left);
            _left -= count;
            return count;
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
            gen.writeName("bin");
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
            gen.writeName("bin");
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
            gen.writeName("bin");
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
            gen.writeName("bin");
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
        _testLargePayload(Base64Variants.MIME_NO_LINEFEEDS, Integer.MAX_VALUE);
        // and same for a variant that does use linefeeds: chunk boundaries must
        // align with line boundaries, or lines would end up longer than allowed
        _testLargePayload(Base64Variants.MIME, 76);
        _testLargePayload(Base64Variants.PEM, 64);
    }

    private void _testLargePayload(Base64Variant b64v, int maxLineLength) throws Exception
    {
        final byte[] big = new byte[7001];
        for (int i = 0; i < big.length; i++) {
            big[i] = (byte) i;
        }
        // Streaming output must match what the `byte[]` overload produces
        final String expected = _writeElement(b64v, big);
        final String actual = _writeElement(b64v, big, -1);
        assertEquals(expected, actual);
        assertEquals(expected, _writeElement(b64v, big, big.length));
        assertEquals(_writeAttribute(b64v, big, big.length), _writeAttribute(b64v, big, -1));

        // and no line may exceed the maximum length variant specifies
        String encoded = actual.substring("<root><bin>".length(), actual.length() - "</bin></root>".length());
        for (String line : encoded.split("\n", -1)) {
            if (line.length() > maxLineLength) {
                fail("Line length "+line.length()+" exceeds max "+maxLineLength+" for "+b64v.getName());
            }
        }

        // and finally, make sure it decodes back
        assertArrayEquals(big, b64v.decode(encoded));
    }

    @Test
    public void testShortReadsUnknownLength() throws Exception
    {
        StringWriter out = new StringWriter();
        try (ToXmlGenerator gen = (ToXmlGenerator) MAPPER.createGenerator(out)) {
            gen.setNextName(new QName("root"));
            gen.writeStartObject();
            gen.writeName("bin");
            assertEquals(DATA.length, gen.writeBinary(Base64Variants.MIME,
                    new ShortReadInputStream(new ByteArrayInputStream(DATA)), -1));
            gen.writeEndObject();
        }
        assertEquals("<root><bin>" + ENCODED + "</bin></root>", removeSjsxpNamespace(out.toString()));
    }

    // Failure partway through an unknown-length read must not leave recycled buffers
    // in a bad state: following writes must still produce correct output
    @Test
    public void testFailingStreamReleasesBuffers() throws Exception
    {
        for (int i = 0; i < 3; ++i) {
            StringWriter out = new StringWriter();
            try (ToXmlGenerator gen = (ToXmlGenerator) MAPPER.createGenerator(out)) {
                gen.setNextName(new QName("root"));
                gen.writeStartObject();
                gen.setNextIsAttribute(true);
                gen.writeName("bin");
                assertThrows(JacksonIOException.class, () -> gen.writeBinary(Base64Variants.MIME,
                        new FailingInputStream(5000), -1));
            } catch (JacksonException e) {
                // close() may fail due to incomplete output; ignore
            }
            // and then verify that buffer recycling still works as expected
            assertEquals("<root><bin>" + ENCODED + "</bin></root>", _writeElement(
                    Base64Variants.MIME, DATA, -1));
        }
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
            gen.writeName("bin");
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
            gen.writeName("bin");
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
            gen.writeName("bin");
            assertEquals(data.length,
                    gen.writeBinary(b64v, new ByteArrayInputStream(data), dataLength));
            gen.writeEndObject();
        }
        return removeSjsxpNamespace(out.toString());
    }
}
