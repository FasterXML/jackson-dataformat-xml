package com.fasterxml.jackson.dataformat.xml;

import java.util.*;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class TestIndentation extends XmlTestBase
{
    /*
    /**********************************************************
    /* Helper types
    /**********************************************************
     */

    static class StringWrapperBean {
        public StringWrapper string;
        
        public StringWrapperBean() { }
        public StringWrapperBean(String s) { string = new StringWrapper(s); }
    }

    static class IntWrapperBean {
        public IntWrapper wrapped;
        
        public IntWrapperBean() { }
        public IntWrapperBean(int i) { wrapped = new IntWrapper(i); }
    }

    // [Issue#45]
    static class AttrBean {
        @JacksonXmlProperty(isAttribute=true)
        public int count = 3;
    }

    static class AttrBean2 {
        @JacksonXmlProperty(isAttribute=true)
        public int count = 3;

        public int value = 14;
    }
    
    /*
    /**********************************************************
    /* Set up
    /**********************************************************
     */

    protected XmlMapper _xmlMapper;

    // let's actually reuse XmlMapper to make things bit faster
    @Override
    public void setUp() throws Exception {
        super.setUp();
        _xmlMapper = new XmlMapper();
        _xmlMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    }

    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

    // Verify [JACKSON-444], Issue #1
    public void testSimpleStringBean() throws Exception
    {
        String xml = _xmlMapper.writeValueAsString(new StringWrapperBean("abc")); 
        // should have at least one linefeed, space...
        if (xml.indexOf('\n') < 0 || xml.indexOf(' ') < 0) {
        	fail("No indentation: XML == "+xml);
        }
        // Let's verify we get similar stuff back, first:
        StringWrapperBean result = _xmlMapper.readValue(xml, StringWrapperBean.class);
        assertNotNull(result);
        assertEquals("abc", result.string.str);

    }

    public void testSimpleIntBean() throws Exception
    {
        String xml = _xmlMapper.writeValueAsString(new IntWrapperBean(42)); 
        // should have at least one linefeed, space...
        if (xml.indexOf('\n') < 0 || xml.indexOf(' ') < 0) {
        	fail("No indentation: XML == "+xml);
        }
        // Let's verify we get similar stuff back, first:
        IntWrapperBean result = _xmlMapper.readValue(xml, IntWrapperBean.class);
        assertNotNull(result);
        assertEquals(42, result.wrapped.i);
    }
    
    public void testSimpleMap() throws Exception
    {
        Map<String,String> map = new HashMap<String,String>();
        map.put("a", "b");
        String xml = _xmlMapper.writeValueAsString(map);

        // should have at least one linefeed, space...
        if (xml.indexOf('\n') < 0 || xml.indexOf(' ') < 0) {
            fail("No indentation: XML == "+xml);
        }
        
        // Let's verify we get similar stuff back, first:
        Map<?,?> result = _xmlMapper.readValue(xml, Map.class);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("b", map.get("a"));
    }

    // [Issue#45]: Use of attributes should not force linefeed for empty elements
    public void testWithAttr() throws Exception
    {
        String xml = _xmlMapper.writeValueAsString(new AttrBean());
        assertEquals("<AttrBean count=\"3\"/>", xml);
        String xml2 = _xmlMapper.writeValueAsString(new AttrBean2());
        assertEquals("<AttrBean2 count=\"3\">\n  <value>14</value>\n</AttrBean2>", xml2);
    }
}