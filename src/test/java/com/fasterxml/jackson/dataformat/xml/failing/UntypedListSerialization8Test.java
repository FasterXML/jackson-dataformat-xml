package com.fasterxml.jackson.dataformat.xml.failing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.XmlTestBase;

public class UntypedListSerialization8Test extends XmlTestBase
{
    @JsonRootName("L")
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

    @JsonRootName("L")
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

    private final XmlMapper MAPPER = newMapper();

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
