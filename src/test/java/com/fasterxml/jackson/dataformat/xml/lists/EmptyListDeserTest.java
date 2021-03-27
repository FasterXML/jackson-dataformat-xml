package com.fasterxml.jackson.dataformat.xml.lists;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;

import com.fasterxml.jackson.dataformat.xml.*;
import com.fasterxml.jackson.dataformat.xml.annotation.*;
import com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser;

public class EmptyListDeserTest extends XmlTestBase
{
    // for [dataformat-xml#124]
    public static class TestList124 {
        @JsonProperty("list")
        public List<Object> list;
    }

    // [dataformat-xml#177]
    static class Config
    {
        @JacksonXmlProperty(isAttribute=true)
        public String id;
        
        @JacksonXmlElementWrapper(useWrapping=false)
        public List<Entry> entry;
    }

    static class Entry
    {
        @JacksonXmlProperty(isAttribute=true)
        public String id;
    }

    // [dataformat-xml#319]
    static class Value319 {
        public Long orderId, orderTypeId;
    }    

    // [dataformat-xml#460]
    static class Channel460 {
        public String channelId;
    }

    static class ChannelSet460 {
        public String setId;

        // is default but just for readability
        @JacksonXmlElementWrapper(useWrapping = true)
        public List<Channel460> channels;
    }

    /*
    /**********************************************************
    /* Test methods
    /**********************************************************
     */

    private final XmlMapper MAPPER = newMapper();

    // [dataformat-xml#124]
    public  void test124() throws Exception {
        TestList124 originalObject = new TestList124();
        originalObject.list = new ArrayList<Object>();
        String xml = MAPPER.writeValueAsString(originalObject);

        TestList124 result = MAPPER.readValue(xml, TestList124.class);

        assertNotNull(result.list);
        assertEquals(0, result.list.size());
    }

    // [dataformat-xml#177]
    public void testEmptyList() throws Exception
    {
        Config r = MAPPER.readValue(
                "<Config id='123'>\n"+
                "  <entry id='foo'> </entry>\n"+
                "</Config>\n",
                Config.class);
        assertNotNull(r);
        assertEquals("123", r.id);
        assertNotNull(r.entry);
        assertEquals(1, r.entry.size());
        assertEquals("foo", r.entry.get(0).id);
    }

    // [dataformat-xml#319]
    public void testEmptyList319() throws Exception
    {
        final String DOC = "<orders></orders>";

        List<Value319> list = MAPPER.readValue(DOC, new TypeReference<List<Value319>>() { });
        assertNotNull(list);
        assertEquals(0, list.size());

        Object result = MAPPER.readValue(DOC, Value319[].class);
        assertNotNull(result);
        assertEquals(Value319[].class, result.getClass());
        Value319[] array = (Value319[]) result;
        assertEquals(0, array.length);
    }

    // [dataformat-xml#435]
    public void testEmptyListAsNull435() throws Exception
    {
        XmlMapper mapper = mapperBuilder()
                .enable(FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL)
                .build();
        List<Config> result = mapper.readValue("<ArrayList/>",
                new TypeReference<List<Config>>() {
        });
        assertNull(result);
    }

    // [dataformat-xml#460]
    public void testWrappedEmptyListWithWhitespace458() throws Exception
    {
        String input = "<ChannelSet460>\n" +
                "<setId>2</setId>\n" +
                "<channels>\n" +
                "</channels>\n" +
                "</ChannelSet460>";
        ChannelSet460 set = MAPPER.readValue(input, ChannelSet460.class);
        assertEquals("List should be empty", 0,
                set.channels.size());
    }
}
