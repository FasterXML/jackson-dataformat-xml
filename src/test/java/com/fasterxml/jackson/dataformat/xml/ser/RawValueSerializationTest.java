package com.fasterxml.jackson.dataformat.xml.ser;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RawValueSerializationTest extends XmlTestUtil
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

    @Test
    public void testRawValueSerialization() throws Exception
    {
        assertEquals("<RawWrapper>"
                +"<id>42</id>"
                +"<raw>Mixed <b>content</b></raw>"
                +"</RawWrapper>",
                MAPPER.writeValueAsString(new RawWrapper()).trim());
    }
}
