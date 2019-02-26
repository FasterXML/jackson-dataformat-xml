package com.fasterxml.jackson.dataformat.xml.misc;

import java.io.*;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.*;
import com.fasterxml.jackson.dataformat.xml.testutil.PrefixInputDecorator;
import com.fasterxml.jackson.dataformat.xml.testutil.PrefixOutputDecorator;

public class StreamingDecoratorsTest extends XmlTestBase
{
    @JsonRootName("wrapper")
    static class Value {
        public String value = "all";
    }
    
    @SuppressWarnings("unchecked")
    public void testInputDecorators() throws IOException
    {
        final byte[] DOC = utf8Bytes("<secret: mum\n");
        final XmlMapper mapper = mapperBuilder(
                streamFactoryBuilder().inputDecorator(new PrefixInputDecorator(DOC))
                .build())
                .build();
        Map<String,Object> value = mapper.readValue(utf8Bytes("value: foo\n"), Map.class);
        assertEquals(2, value.size());
        assertEquals("foo", value.get("value"));
        assertEquals("mum", value.get("secret"));

        // and then via Reader as well
        value = mapper.readValue(new StringReader("value: xyz\n"), Map.class);
        assertEquals(2, value.size());
        assertEquals("xyz", value.get("value"));
        assertEquals("mum", value.get("secret"));
    }

    public void testOutputDecorators() throws IOException
    {
        final String PREFIX = "///////";
        final byte[] DOC = utf8Bytes(PREFIX);
        final XmlMapper mapper = mapperBuilder(
                streamFactoryBuilder().outputDecorator(new PrefixOutputDecorator(DOC))
                .build())
                .build();
        final Value input = new Value();

        // Gets bit tricky because writer will add doc prefix. So let's do simpler check here

        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
            mapper.writeValue(bytes, input);
    
            String raw = bytes.toString("UTF-8");
            if (!raw.startsWith(PREFIX)) {
                fail("Should start with prefix, did not: ["+raw+"]");
            }
        }

        // and same with char-backed too
        try (StringWriter sw = new StringWriter()) {
            mapper.writeValue(sw, input);
            String raw = sw.toString();
            if (!raw.startsWith(PREFIX)) {
                fail("Should start with prefix, did not: ["+raw+"]");
            }
        }
    }
}
