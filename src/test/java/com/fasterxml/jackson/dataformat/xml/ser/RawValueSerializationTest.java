package com.fasterxml.jackson.dataformat.xml.ser;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

// [dataformat-xml#269]
public class RawValueSerializationTest extends XmlTestBase
{
    @JsonPropertyOrder({ "id", "raw" })
    static class RawWrapper {
        public int id = 42;

        @JsonRawValue
        public String raw = "Mixed <b>content</b>";
    }

    /*
    /********************************************************
    /* Test methods
    /********************************************************
     */

    private final XmlMapper MAPPER = newMapper();

    public void testRawValueSerialization() throws Exception
    {
        assertEquals("<RawWrapper>"
                +"<id>42</id>"
                +"<raw>Mixed <b>content</b></raw>"
                +"</RawWrapper>",
                MAPPER.writeValueAsString(new RawWrapper()).trim());
    }
}
