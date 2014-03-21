package com.fasterxml.jackson.dataformat.xml.ser;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import com.fasterxml.jackson.dataformat.xml.*;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class TestSerializationOrdering extends XmlTestBase
{
    @JsonPropertyOrder({"a", "c" })
    static class Bean91 {
        public String a;
        @JacksonXmlProperty(isAttribute = true)
        public String b;
        public String c;

        public Bean91(String a, String b, String c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }
    }

    public void testOrdering() throws Exception
    {
        XmlMapper xmlMapper = new XmlMapper();
        String xml = xmlMapper.writeValueAsString(new Bean91("1", "2", "3"));
        assertEquals("<Bean91 b=\"2\"><a>1</a><c>3</c></Bean91>", xml);
    }
}
