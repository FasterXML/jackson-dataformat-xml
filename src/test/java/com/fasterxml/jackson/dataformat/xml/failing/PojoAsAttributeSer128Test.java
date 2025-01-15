package com.fasterxml.jackson.dataformat.xml.failing;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;

import com.fasterxml.jackson.dataformat.xml.*;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import static org.junit.jupiter.api.Assertions.*;

// [dataformat-xml#128]: Should ignore "as-attribute" setting for POJO
public class PojoAsAttributeSer128Test extends XmlTestUtil
{
    static class Bean {
        public int value = 42;
    }

    @JsonRootName("root")
    @JsonPropertyOrder({ "start", "bean", "end" })
    static class Container {
        @JacksonXmlProperty(isAttribute = true)
        public String start = "!start";

        @JacksonXmlProperty(isAttribute = true)
        protected Bean bean = new Bean();

        @JacksonXmlProperty(isAttribute = true)
        public String end = "!end";
    }

    /*
    /********************************************************
    /* Test methods
    /********************************************************
     */

    private final XmlMapper MAPPER = newMapper();

    @Test
    public void testAttributeDeser128() throws Exception
    {
        final String output = MAPPER.writeValueAsString(new Container()).trim();
//System.err.println(output);
        assertEquals("<root start=\"!start\" end=\"!end\"><bean><value>42</value></bean></root>",
                output);
    }
}
