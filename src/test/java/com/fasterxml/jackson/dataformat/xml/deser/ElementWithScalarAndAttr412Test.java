package com.fasterxml.jackson.dataformat.xml.deser;

import java.net.URI;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class ElementWithScalarAndAttr412Test extends XmlTestBase
{
    @JsonRootName("container")
    static class Bean412 {
        @JacksonXmlProperty(localName = "values")
        @JacksonXmlElementWrapper(useWrapping = false)
        List<TaggedInt412> v;
    }

    @JsonRootName("tagged")
    static class TaggedInt412 {
        @JacksonXmlProperty
        public int id;

        @JacksonXmlProperty
        public int count;
    }

    @JsonRootName("tagged")
    static class TaggedBoolean412 {
        @JacksonXmlProperty
        public Boolean truthy;

        @JacksonXmlProperty
        public int count;
    }

    @JsonRootName("tagged")
    static class TaggedBooleanPrim412 {
        @JacksonXmlProperty
        public boolean truthy;

        @JacksonXmlProperty
        public int count;
    }
    
    @JsonRootName("tagged")
    static class TaggedDouble412 {
        @JacksonXmlProperty
        public Double value;

        @JacksonXmlProperty
        public int count;
    }

    @JsonRootName("tagged")
    static class TaggedDoublePrim412 {
        @JacksonXmlProperty
        public double value;

        @JacksonXmlProperty
        public int count;
    }

    @JsonRootName("tagged")
    static class TaggedString412 {
        @JacksonXmlProperty
        public String name;

        @JacksonXmlProperty
        public int count;
    }

    @JsonRootName("tagged")
    static class TaggedURI412 {
        @JacksonXmlProperty
        public URI location;

        @JacksonXmlProperty
        public int count;
    }
    
    @JsonRootName("tagged")
    static class TaggedDate412 {
        @JacksonXmlProperty
        public Date time;

        @JacksonXmlProperty
        public int count;
    }

    /*
    /**********************************************************
    /* Test methods, main tests
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
                + "        <id arg='oof'>\n"
                + "               1235\n"
                + "        </id>\n"
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
        TaggedInt412 result;
        String XML = "<tagged>\n"
                + "        <id bar='baz'>2812</id>\n"
                + "        <count>15</count>\n"
                + "</tagged>";
        result = MAPPER.readValue(XML, TaggedInt412.class);
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

    /*
    /**********************************************************
    /* Test methods, simpler for other types
    /**********************************************************
     */

    public void testBooleanFromElemAndAttr() throws Exception
    {
        final String XML = "<tagged><truthy bar='baz'>true</truthy><count>3</count></tagged>";
        {
            TaggedBoolean412 result = MAPPER.readValue(XML, TaggedBoolean412.class);
            assertEquals(Boolean.TRUE, result.truthy);
            assertEquals(3, result.count);
        }
        {
            TaggedBooleanPrim412 result = MAPPER.readValue(XML, TaggedBooleanPrim412.class);
            assertTrue(result.truthy);
            assertEquals(3, result.count);
        }
    }

    public void testDoubleFromElemAndAttr() throws Exception
    {
        final String XML = "<tagged><count>28</count><value bar='baz'>  0.25 </value></tagged>";
        {
            TaggedDouble412 result = MAPPER.readValue(XML, TaggedDouble412.class);
            assertEquals(Double.valueOf(0.25), result.value);
            assertEquals(28, result.count);
        }

        {
            TaggedDoublePrim412 result = MAPPER.readValue(XML, TaggedDoublePrim412.class);
            assertEquals(0.25, result.value);
            assertEquals(28, result.count);
        }
    }

    public void testStringFromElemAndAttr() throws Exception
    {
        TaggedString412 result = MAPPER.readValue(
                "<tagged><name bar='baz'>Poobah</name><count>7</count></tagged>",
                TaggedString412.class);
        assertEquals("Poobah", result.name);
        assertEquals(7, result.count);
    }

    public void testURIFromElemAndAttr() throws Exception
    {
        TaggedURI412 result = MAPPER.readValue(
                "<tagged><location bar='baz'>\n"
                +"    http://foo.bar\n"
                +"</location><count>11</count></tagged>",
                TaggedURI412.class);
        assertEquals(URI.create("http://foo.bar"), result.location);
        assertEquals(11, result.count);
    }

    public void testDateFromElemAndAttr() throws Exception
    {
        TaggedDate412 result = MAPPER.readValue(
                "<tagged><time bar='baz'>\n"
                +"2020-06-20T17:00:20Z"
                +"</time><count>11</count></tagged>",
                TaggedDate412.class);
        assertNotNull(result.time);
        assertEquals(11, result.count);
    }
}

