package com.fasterxml.jackson.dataformat.xml.ser;

import java.io.*;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@SuppressWarnings("serial")
public class TestSerialization extends XmlTestBase
{
    static class StringBean2
    {
        public String text = "foobar";
    }

    static class AttributeBean
    {
        @JacksonXmlProperty(isAttribute=true, localName="attr")
        public String text = "something";
    }

    static class AttrAndElem
    {
        public String elem = "whatever";
        
        @JacksonXmlProperty(isAttribute=true, localName="id")
        public int attr = 42;
    }

    static class WrapperBean<T>
    {
        public T value;

        public WrapperBean() { }
        public WrapperBean(T v) { value = v; }
    }

    static class MapBean
    {
        public Map<String,Integer> map;

        public MapBean() { }
        public MapBean(Map<String,Integer> v) { map = v; }
    }

    static class NsElemBean
    {
        @JacksonXmlProperty(namespace="http://foo")
        public String text = "blah";
    }

    static class NsElemBean2
    {
        @JsonProperty(namespace="http://foo")
        public String text = "blah";
    }
    
    static class CDataStringBean
    {
        @JacksonXmlCData
        public String value = "<some<data\"";
    }

    static class CDataStringArrayBean
    {
        @JacksonXmlCData
        public String[] value = {"<some<data\"", "abc"};
    }

    static class CustomMap extends LinkedHashMap<String, Integer> { }

    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

    private final XmlMapper _xmlMapper = new XmlMapper();

    public void testSimpleAttribute() throws IOException
    {
        String xml = _xmlMapper.writeValueAsString(new AttributeBean());
        xml = removeSjsxpNamespace(xml);
        assertEquals("<AttributeBean attr=\"something\"/>", xml);
    }

    public void testSimpleNsElem() throws IOException
    {
        String xml = _xmlMapper.writeValueAsString(new NsElemBean());
        xml = removeSjsxpNamespace(xml);
        // here we assume woodstox automatic prefixes, not very robust but:
        assertEquals("<NsElemBean><wstxns1:text xmlns:wstxns1=\"http://foo\">blah</wstxns1:text></NsElemBean>", xml);
    }

    public void testSimpleNsElemWithJsonProp() throws IOException
    {
        String xml = _xmlMapper.writeValueAsString(new NsElemBean2());
        xml = removeSjsxpNamespace(xml);
        // here we assume woodstox automatic prefixes, not very robust but:
        assertEquals("<NsElemBean2><wstxns1:text xmlns:wstxns1=\"http://foo\">blah</wstxns1:text></NsElemBean2>", xml);
    }
    
    public void testSimpleAttrAndElem() throws IOException
    {
        String xml = _xmlMapper.writeValueAsString(new AttrAndElem());
        xml = removeSjsxpNamespace(xml);
        assertEquals("<AttrAndElem id=\"42\"><elem>whatever</elem></AttrAndElem>", xml);
    }

    public void testMap() throws IOException
    {
        // First, map in a general wrapper
        LinkedHashMap<String,Integer> map = new LinkedHashMap<String,Integer>();
        map.put("a", 1);
        map.put("b", 2);

        String xml;
        
        xml = _xmlMapper.writeValueAsString(new WrapperBean<Map<?,?>>(map));
        assertEquals("<WrapperBean><value>"
                +"<a>1</a>"
                +"<b>2</b>"
                +"</value></WrapperBean>",
                xml);

        // then as strongly typed
        xml = _xmlMapper.writeValueAsString(new MapBean(map));
        assertEquals("<MapBean><map>"
                +"<a>1</a>"
                +"<b>2</b>"
                +"</map></MapBean>",
                xml);
    }

    public void testNakedMap() throws IOException
    {
        CustomMap input = new CustomMap();        
        input.put("a", 123);
        input.put("b", 456);
        String xml = _xmlMapper.writeValueAsString(input);

        
//        System.err.println("XML = "+xml);
        
        CustomMap result = _xmlMapper.readValue(xml, CustomMap.class);
        assertEquals(2, result.size());

        assertEquals(Integer.valueOf(456), result.get("b"));
    }

    public void testCDataString() throws IOException
    {
        String xml = _xmlMapper.writeValueAsString(new CDataStringBean());
        xml = removeSjsxpNamespace(xml);
        assertEquals("<CDataStringBean><value><![CDATA[<some<data\"]]></value></CDataStringBean>", xml);
    }

    public void testCDataStringArray() throws IOException
    {
        String xml = _xmlMapper.writeValueAsString(new CDataStringArrayBean());
        xml = removeSjsxpNamespace(xml);
        assertEquals("<CDataStringArrayBean><value><value><![CDATA[<some<data\"]]></value><value><![CDATA[abc]]></value></value></CDataStringArrayBean>", xml);
    }

    // manual 'test' to see "what would JAXB do?"
    /*
    public void testJAXB() throws Exception
    {
        StringWriter sw = new StringWriter();
        jakarta.xml.bind.JAXB.marshal(new StringListBean("a", "b", "c"), sw);
        System.out.println("JAXB -> "+sw);
    }
    */
}
