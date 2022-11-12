package com.fasterxml.jackson.dataformat.xml.ser;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import com.fasterxml.jackson.dataformat.xml.*;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class TestSerializationOrdering extends XmlTestBase
{
    @JsonPropertyOrder({"a", "c" })
    static class Bean91 {
        public String c;
        @JacksonXmlProperty(isAttribute = true)
        public String b;
        public String a;

        public Bean91(String a, String b, String c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }
    }

    private final XmlMapper MAPPER = newMapper();

    public void testOrdering() throws Exception
    {
        String xml = MAPPER.writeValueAsString(new Bean91("1", "2", "3"));
        assertEquals("<Bean91 b=\"2\"><a>1</a><c>3</c></Bean91>", xml);
    }
}
