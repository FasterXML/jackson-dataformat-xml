package com.fasterxml.jackson.dataformat.xml.failing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;


import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.*;

import static org.junit.jupiter.api.Assertions.*;

public class UntypedListSerialization8Test extends XmlTestUtil
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
    @Test
    public void testListAsObject() throws IOException
    {
        String xmlForUntyped = MAPPER.writeValueAsString(new UntypedListBean());
        String xmlForTyped = MAPPER.writeValueAsString(new TypedListBean());

        assertEquals(xmlForTyped, xmlForUntyped);
    }
}
