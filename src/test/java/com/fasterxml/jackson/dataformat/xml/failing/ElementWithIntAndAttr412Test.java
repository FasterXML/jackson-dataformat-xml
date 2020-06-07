package com.fasterxml.jackson.dataformat.xml.failing;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

public class ElementWithIntAndAttr412Test extends XmlTestBase
{
    @JacksonXmlRootElement(localName = "container")
    static class Bean412 {
        @JacksonXmlProperty(localName = "values")
        @JacksonXmlElementWrapper(useWrapping = false)
        List<TaggedInt412> v;
    }

    @JacksonXmlRootElement(localName = "tagged")
    static class TaggedInt412 {
        @JacksonXmlProperty
        public int id;

        @JacksonXmlProperty
        public int count;
    }

    /*
    /**********************************************************
    /* Test methods
    /**********************************************************
     */

    private final ObjectMapper MAPPER = newMapper();
    
    // [dataformat-xml#412]
    public void testIntFromElemAndAttrInList() throws Exception
    {
        String XML = "<container>\n"
                + "    <values>\n"
                + "        <id bar='baz'>2812</id>\n"
                + "        <count>15</count>\n"
                + "    </values>\n"
                + "    <values>\n"
                + "        <count>42</count>\n"
                + "        <id arg='oof'>1235</id>\n"
                + "    </values>\n"
                + "</container>";
        Bean412 many = MAPPER.readValue(XML, Bean412.class);
        assertNotNull(many.v);
//System.err.println("XML:\n"+MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(many));
        assertEquals(2, many.v.size());
        assertEquals(2812, many.v.get(0).id);
        assertEquals(15, many.v.get(0).count);
        assertEquals(1235, many.v.get(1).id);
        assertEquals(42, many.v.get(1).count);
    }

    public void testIntFromElemAndAttr() throws Exception
    {
        String XML = "<tagged>\n"
                + "        <id bar='baz'>2812</id>\n"
                + "        <count>15</count>\n"
                + "</tagged>";
        TaggedInt412 result = MAPPER.readValue(XML, TaggedInt412.class);
        assertEquals(2812, result.id);
        assertEquals(15, result.count);

        XML = "    <tagged>\n"
            + "        <count>42</count>\n"
            + "        <id arg='oof'>1235</id>\n"
            + "    </tagged>\n";
    
        result = MAPPER.readValue(XML, TaggedInt412.class);
        assertEquals(1235, result.id);
        assertEquals(42, result.count);
    }
}
