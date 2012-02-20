package com.fasterxml.jackson.xml;

import java.io.*;
import java.util.*;


import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotate.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotate.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotate.JacksonXmlRootElement;

public class TestSerialization extends XmlTestBase
{
    /*
    /**********************************************************
    /* Helper types
    /**********************************************************
     */

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

    static class ListBean
    {
        public final List<Integer> values = new ArrayList<Integer>();

        public ListBean() { }
        public ListBean(int... ints) {
            for (int i : ints) {
                values.add(Integer.valueOf(i));
            }
        }
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
    
    static class StringListBean
    {
        // to see what JAXB gives, uncomment:
        //@javax.xml.bind.annotation.XmlElementWrapper(name="stringList")
        @JacksonXmlElementWrapper(localName="stringList")
        public List<StringBean> strings;
        
        public StringListBean() { strings = new ArrayList<StringBean>(); }
        public StringListBean(String... texts)
        {
            strings = new ArrayList<StringBean>();
            for (String text : texts) {
                strings.add(new StringBean(text));
            }
        }
    }
    
    static class NsElemBean
    {
        @JacksonXmlProperty(namespace="http://foo")
        public String text = "blah";
    }

    static class NsAttrBean
    {
        @JacksonXmlProperty(namespace="http://foo", isAttribute=true)
        public String attr = "3";
    }

    @JacksonXmlRootElement(localName="root")
    static class RootBean
    {
        public String value = "123";
    }

    @JacksonXmlRootElement(localName="nsRoot", namespace="http://foo")
    static class NsRootBean
    {
        public String value = "abc";
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
    }
    
    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

    // Unit test to verify that root name is properly set
    public void testRootName() throws IOException
    {
        String xml = _xmlMapper.writeValueAsString(new StringBean());
        
        // Hmmh. Looks like JDK Stax may adds bogus ns declaration. As such,
        // let's just check that name starts ok...
        if (!xml.startsWith("<StringBean")) {
            fail("Expected root name of 'StringBean'; but XML document is ["+xml+"]");
        }

        // and then see that basic non-namespace root is ok
        xml = _xmlMapper.writeValueAsString(new RootBean());
        assertEquals("<root><value>123</value></root>", xml);

        // and namespace one too
        xml = _xmlMapper.writeValueAsString(new NsRootBean());
        if (xml.indexOf("nsRoot") < 0) { // verify localName
            fail("Expected root name of 'nsRoot'; but XML document is ["+xml+"]");
        }
        // and NS declaration
        if (xml.indexOf("http://foo") < 0) {
            fail("Expected NS declaration for 'http://foo', not found, XML document is ["+xml+"]");
        }
    }
    
    public void testSimpleAttribute() throws IOException
    {
        String xml = _xmlMapper.writeValueAsString(new AttributeBean());
        xml = removeSjsxpNamespace(xml);
        assertEquals("<AttributeBean attr=\"something\"/>", xml);
    }

    public void testSimpleAttrAndElem() throws IOException
    {
        String xml = _xmlMapper.writeValueAsString(new AttrAndElem());
        xml = removeSjsxpNamespace(xml);
        assertEquals("<AttrAndElem id=\"42\"><elem>whatever</elem></AttrAndElem>", xml);
    }

    public void testSimpleNsElem() throws IOException
    {
        String xml = _xmlMapper.writeValueAsString(new NsElemBean());
        xml = removeSjsxpNamespace(xml);
        // here we assume woodstox automatic prefixes, not very robust but:
        assertEquals("<NsElemBean><wstxns1:text xmlns:wstxns1=\"http://foo\">blah</wstxns1:text></NsElemBean>", xml);
    }

    public void testSimpleNsAttr() throws IOException
    {
        String xml = _xmlMapper.writeValueAsString(new NsAttrBean());
        xml = removeSjsxpNamespace(xml);
        // here we assume woodstox automatic prefixes, not very robust but:
        assertEquals("<NsAttrBean xmlns:wstxns1=\"http://foo\" wstxns1:attr=\"3\"/>", xml);
    }
    
    public void testSimpleList() throws IOException
    {
        String xml = _xmlMapper.writeValueAsString(new ListBean(1, 2, 3));
        xml = removeSjsxpNamespace(xml);
        // 06-Dec-2010, tatu: Not completely ok; should default to not using wrapper...
        assertEquals("<ListBean><values><values>1</values><values>2</values><values>3</values></values></ListBean>", xml);
    }

    public void testStringList() throws IOException
    {
        StringListBean list = new StringListBean("a", "b", "c");
        String xml = _xmlMapper.writeValueAsString(list);
        xml = removeSjsxpNamespace(xml);
        // 06-Dec-2010, tatu: Not completely ok; should default to not using wrapper... but it's what we have now
        assertEquals("<StringListBean><stringList>"
                +"<strings><text>a</text></strings>"
                +"<strings><text>b</text></strings>"
                +"<strings><text>c</text></strings>"
                +"</stringList></StringListBean>", xml);
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
    
    // manual 'test' to see "what would JAXB do?"
    /*
    public void testJAXB() throws Exception
    {
        StringWriter sw = new StringWriter();
        javax.xml.bind.JAXB.marshal(new StringListBean("a", "b", "c"), sw);
        System.out.println("JAXB -> "+sw);
    }
    */
}
