package com.fasterxml.jackson.dataformat.xml;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

public class TestSerializationAttr extends XmlTestBase
{
    static class NsAttrBean
    {
        @JacksonXmlProperty(namespace="http://foo", isAttribute=true)
        public String attr = "3";
    }

    @JacksonXmlRootElement(localName="test", namespace="http://root")
    static class Issue19Bean
    {
    	@JsonProperty
        @JacksonXmlProperty(namespace = "http://my.ns")
    	public boolean booleanA = true;

    	@JsonProperty
        @JacksonXmlProperty(isAttribute=true)
    	public String id = "abc";
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

    public void testSimpleNsAttr() throws IOException
    {
        String xml = _xmlMapper.writeValueAsString(new NsAttrBean());
        xml = removeSjsxpNamespace(xml);
        // here we assume woodstox automatic prefixes, not very robust but:
        assertEquals("<NsAttrBean xmlns:wstxns1=\"http://foo\" wstxns1:attr=\"3\"/>", xml);
    }

    public void testIssue19() throws IOException
    {
        String xml = _xmlMapper.writeValueAsString(new Issue19Bean());
        xml = removeSjsxpNamespace(xml);
        xml = xml.replaceAll("\"", "'");
        // as with above, assumes exact NS allocation strategy, not optimal:
        assertEquals("<wstxns1:test xmlns:wstxns1='http://root' id='abc'>"
        		+"<wstxns2:booleanA xmlns:wstxns2='http://my.ns'>true</wstxns2:booleanA></wstxns1:test>",
        	xml);
    }
    
    /*
    /**********************************************************
    /* Helper methods
    /**********************************************************
     */
    
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
