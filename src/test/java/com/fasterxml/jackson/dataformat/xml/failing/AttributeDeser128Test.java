package com.fasterxml.jackson.dataformat.xml.failing;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;


// [dataformat-xml#128]: Should ignore "as-attribute" setting for POJO
public class AttributeDeser128Test extends XmlTestBase
{
    static class Bean {
        public int value = 42;
    }

    @JacksonXmlRootElement(localName="root")
    @JsonPropertyOrder({ "start", "bean", "end" })
    static class Container {
        @JacksonXmlProperty(isAttribute = true)
        public String start = "!start";
        
        @JacksonXmlProperty(isAttribute = true)
        public String end = "!end";

        @JacksonXmlProperty(isAttribute = true)
        protected Bean bean = new Bean();
    }

    /*
    /********************************************************
    /* Test methods
    /********************************************************
     */

    private final XmlMapper MAPPER = newMapper();

    public void testAttributeDeser128() throws Exception
    {
        assertEquals("<root start=\"!start\" end=\"!end\"><bean><value>42</value></bean></root>",
                MAPPER.writeValueAsString(new Container()).trim());
    }
}
