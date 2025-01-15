package com.fasterxml.jackson.dataformat.xml.deser;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ExceptionDeserializationTest extends XmlTestUtil
{
    /*
    /**********************************************************
    /* Test methods
    /**********************************************************
     */

    private final XmlMapper MAPPER = new XmlMapper();

    // [dataformat-xml#250]
    @Test
    public void testEmptyString162() throws Exception
    {
        IOException src = new IOException("test");
        String xml = MAPPER.writeValueAsString(src);
        IOException e = MAPPER.readValue(xml, IOException.class);
        assertNotNull(e);
        assertEquals("test", e.getMessage());
    }
}
