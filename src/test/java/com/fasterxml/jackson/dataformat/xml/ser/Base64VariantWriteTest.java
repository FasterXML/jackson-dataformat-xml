package com.fasterxml.jackson.dataformat.xml.ser;

import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

import static org.junit.Assert.*;

public class Base64VariantWriteTest extends XmlTestBase
{
    public static class BinaryValue {
        public byte[] value;

        protected BinaryValue() { }
        public BinaryValue(byte[] v) {
            value = v;
        }
    }

    private final byte[] BINARY_DATA;
    {
        try {
            BINARY_DATA = "abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890X".getBytes("UTF-8");
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    private final static String XML_MIME_NO_LINEFEEDS =
"YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwYWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwWA==";

    private final static String XML_MIME =
"YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwYWJjZGVmZ2hpamtsbW5vcHFyc3R1\n"
+"dnd4eXoxMjM0NTY3ODkwWA==";
    private final static String XML_MOD_FOR_URL =
"YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwYWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwWA";
    private final static String XML_PEM =
"YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwYWJjZGVmZ2hpamts\n"
+"bW5vcHFyc3R1dnd4eXoxMjM0NTY3ODkwWA==";

    private final XmlMapper MAPPER = newMapper();

    public void testBinaryVariantsCompact() throws Exception
    {
        _testBinaryVariants(Base64Variants.MIME, XML_MIME, false);

        _testBinaryVariants(Base64Variants.MIME_NO_LINEFEEDS, XML_MIME_NO_LINEFEEDS, false);
        _testBinaryVariants(Base64Variants.MODIFIED_FOR_URL, XML_MOD_FOR_URL, false);
        _testBinaryVariants(Base64Variants.PEM, XML_PEM, false);

        // default pre-2.12 was "MIME", despite Jackson/json default of "MIME_NO_LINEFEEDS,
        // so kept the same for 2.12 by changing XMLMapper defaults
        _testBinaryVariants(null, XML_MIME, false);
    }

    public void testBinaryVariantsPretty() throws Exception
    {
        _testBinaryVariants(Base64Variants.MIME, XML_MIME, true);

        _testBinaryVariants(Base64Variants.MIME_NO_LINEFEEDS, XML_MIME_NO_LINEFEEDS, true);
        _testBinaryVariants(Base64Variants.MODIFIED_FOR_URL, XML_MOD_FOR_URL, true);
        _testBinaryVariants(Base64Variants.PEM, XML_PEM, true);

        // default pre-2.12 was "MIME", despite Jackson/json default of "MIME_NO_LINEFEEDS,
        // so kept the same for 2.12 by changing XMLMapper defaults
        _testBinaryVariants(null, XML_MIME, true);
    }

    private void _testBinaryVariants(Base64Variant b64v, String expEncoded,
            boolean indent) throws Exception
    {
        ObjectWriter w = MAPPER.writer();
        if (indent) {
            w = w.withDefaultPrettyPrinter();
        }
        ObjectReader r = MAPPER.readerFor(BinaryValue.class);
        if (b64v != null) {
            w = w.with(b64v);
            r = r.with(b64v);
        }
        final String EXP = indent ?
                "<BinaryValue>\n  <value>"+expEncoded+"</value>\n</BinaryValue>" :
                "<BinaryValue><value>"+expEncoded+"</value></BinaryValue>";
        final String xml = w.writeValueAsString(new BinaryValue(BINARY_DATA)).trim();

//System.err.println("EXP:\n"+EXP+"\nACT:\n"+xml+"\n");

        assertEquals(EXP, xml);

        // and read back just for shirts & goggles
        BinaryValue result = r.readValue(EXP);
        assertArrayEquals(BINARY_DATA, result.value);
    }
}
