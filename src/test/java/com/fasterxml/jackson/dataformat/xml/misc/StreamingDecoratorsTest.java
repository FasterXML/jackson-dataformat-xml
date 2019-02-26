package com.fasterxml.jackson.dataformat.xml.misc;

import java.io.*;

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

    public void testInputDecorators() throws IOException
    {
        final byte[] DOC = utf8Bytes("<wrapper>\n");
        final XmlMapper mapper = mapperBuilder(
                streamFactoryBuilder().inputDecorator(new PrefixInputDecorator(DOC))
                .build())
                .build();
        Value value = mapper.readValue(utf8Bytes("<value>test</value></wrapper>"), Value.class);
        assertEquals("test", value.value);

        // and then via Reader as well
        value = mapper.readValue(new StringReader("<value>test2</value></wrapper>"), Value.class);
        assertEquals("test2", value.value);
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
