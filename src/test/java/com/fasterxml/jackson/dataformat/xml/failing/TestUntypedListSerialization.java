package com.fasterxml.jackson.dataformat.xml.failing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

public class TestUntypedListSerialization extends XmlTestBase
{
    @JacksonXmlRootElement(localName="L")
    static class UntypedListBean
    {
        public final Object list;
    	
        public UntypedListBean() {
            ArrayList<String> l= new ArrayList<String>();
            l.add("first");
            l.add("second");
            list = l;
        }
    }

    @JacksonXmlRootElement(localName="L")
    static class TypedListBean
    {
        public final List<String> list;
    	
        public TypedListBean() {
            ArrayList<String> l= new ArrayList<String>();
            l.add("first");
            l.add("second");
            list = l;
        }
    }
    
    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

    private final XmlMapper MAPPER = new XmlMapper();

    /*
     *  For [dataformat-xml#8] -- Will not use wrapping, if type is not statically known
     *  to be a Collection
     */
    public void testListAsObject() throws IOException
    {
        String xmlForUntyped = MAPPER.writeValueAsString(new UntypedListBean());
        String xmlForTyped = MAPPER.writeValueAsString(new TypedListBean());

        assertEquals(xmlForTyped, xmlForUntyped);
    }
}
