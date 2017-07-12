package com.fasterxml.jackson.dataformat.xml.deser;

import java.io.IOException;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

public class ExceptionDeserializationTest extends XmlTestBase
{
    /*
    /**********************************************************
    /* Test methods
    /**********************************************************
     */

    private final XmlMapper MAPPER = new XmlMapper();

    // [dataformat-xml#250]
    public void testEmptyString162() throws Exception
    {
        IOException src = new IOException("test");
        String xml = MAPPER.writeValueAsString(src);
        IOException e = MAPPER.readValue(xml, IOException.class);
        assertNotNull(e);
        assertEquals("test", e.getMessage());
    }
}
