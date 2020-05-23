package com.fasterxml.jackson.dataformat.xml.failing;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

public class ListDeser390Test extends XmlTestBase
{

    @JacksonXmlRootElement(localName = "many")
    static class Many390 {
        @JacksonXmlProperty(localName = "one")
        @JacksonXmlElementWrapper(useWrapping = false)
        List<One390> ones;
    }

    static class One390 {
        @JacksonXmlProperty
        String value;
        @JacksonXmlProperty
        String another;
    }

    /*
    /********************************************************
    /* Test methods
    /********************************************************
     */

    private final ObjectMapper MAPPER = newMapper();

    // [dataformat-xml#390]
    public void testDeser390() throws Exception
    {
        String XML = "<many>\n"
                + "    <one>\n"
                + "        <value bar=\"baz\">foo</value>\n"
                + "        <another></another>\n"
                + "    </one>\n"
                + "</many>";
        Many390 many = MAPPER.readValue(XML, Many390.class);
        assertNotNull(many.ones);

//System.err.println("XML:\n"+MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(many));
        assertEquals(1, many.ones.size());
    }
}
